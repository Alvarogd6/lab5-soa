package soa.eip;

import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Router extends RouteBuilder {

  public static final String DIRECT_URI = "direct:twitter";

  private Processor changeBody() {
    return new Processor() {
      public void process(Exchange exchange) throws Exception {
        String payload = exchange.getIn().getBody(String.class);
        String body = "", max = "";
        for(String s : payload.split(" ")) {
          if(s.matches("max:[0-9]+")) max = s.split(":")[1];
          else body += s + " ";
        }
        exchange.getIn().setBody(body);
        exchange.getIn().setHeader("count", max);
      }
    };
  }

  @Override
  public void configure() {
    from(DIRECT_URI)
      .log("Body contains \"${body}\"")
      .log("Searching twitter for \"${body}\"!")
      .choice()
        .when(body().regex(".*max:[0-9]+.*"))
          .process(changeBody())
          .toD("twitter-search:${body}?count=${header.count}")
          .endChoice()
        .otherwise()
          .toD("twitter-search:${body}")
          .endChoice()
      .end()
      .log("Body now contains the response from twitter:\n${body}");
  }
}
