package com.example.postgres2app;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("internalServerErrorProcessor")
public class InternalServerErrorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        exchange.getIn().setBody("Internal server error. Please try again later.");
    }
}
