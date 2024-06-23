package com.example.postgres2app;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("notFoundExceptionProcessor")
public class NotFoundExceptionProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
        exchange.getIn().setBody("Resource not found");
    }
}