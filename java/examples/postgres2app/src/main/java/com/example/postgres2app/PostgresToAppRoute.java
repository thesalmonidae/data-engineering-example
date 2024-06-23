package com.example.postgres2app;

import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;

/**
 * Example REST API for an application that would utilize
 * the data from the Postgres database.
 * 
 * The Netty HTTP is serving the REST API, and then the response
 * is fetched from the Postgres database and returned to the
 * user.
 */
public class PostgresToAppRoute extends RouteBuilder {
    private final NotFoundExceptionProcessor notFoundExceptionProcessor;
    private final InternalServerErrorProcessor internalServerErrorProcessor;

    public PostgresToAppRoute(
            NotFoundExceptionProcessor notFoundExceptionProcessor,
            InternalServerErrorProcessor internalServerErrorProcessor
        ) {
        this.notFoundExceptionProcessor = notFoundExceptionProcessor;
        this.internalServerErrorProcessor = internalServerErrorProcessor;
    }

    @Override
    public void configure() throws Exception {
        errorHandler(deadLetterChannel("direct:errorHandler")
            .useOriginalMessage()
            .logStackTrace(false)
            .maximumRedeliveries(0));

        onException(Exception.class)
            .handled(true)
            .process(internalServerErrorProcessor);

        onException(IllegalArgumentException.class)
            .handled(true)
            .process(notFoundExceptionProcessor);

        from("direct:errorHandler")
            .process(exchange -> {
                Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                if (caused != null) {
                    exchange.getIn().setBody("Internal server error: " + caused.getMessage());
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
                }
            });

        restConfiguration()
        .component("netty-http")
        .host("0.0.0.0")
        .port(8080);

        rest("/api/v1")
            .get("/data")
            .param().name("id").type(RestParamType.query).endParam()
            .param().name("startTimestamp").type(RestParamType.query).endParam()
            .param().name("endTimestamp").type(RestParamType.query).endParam()
            .to("direct:fetchData");

        from("direct:fetchData")
        .process(exchange -> {
            exchange.getIn().getHeaders().forEach((key, value) -> System.out.println(key + ": " + value));
            StringBuilder query = new StringBuilder("SELECT id, TO_CHAR(timestamp, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS') AS timestamp, lon, lat, filtered_lon, filtered_lat FROM iot WHERE 1=1");

            String id = exchange.getIn().getHeader("id", String.class);
            String startTimestamp = exchange.getIn().getHeader("startTimestamp", String.class);
            String endTimestamp = exchange.getIn().getHeader("endTimestamp", String.class);

            if (id != null) {
                query.append(" AND id = :#id");
            }
            if (startTimestamp != null) {
                query.append(" AND timestamp >= CAST(:#startTimestamp AS timestamp)");
            }
            if (endTimestamp != null) {
                query.append(" AND timestamp <= CAST(:#endTimestamp AS timestamp)");
            }

            exchange.getIn().setBody(query.toString());
        })
        .toD("sql:${body}?dataSource=#dataSource&outputClass=com.example.postgres2app.DataPoint")
        .process(exchange -> {
            @SuppressWarnings("unchecked")
            List<DataPoint> result = exchange.getIn().getBody(List.class);
            if (result == null || result.isEmpty()) {
                throw new IllegalArgumentException("ID not found");
            }
        })
        .marshal()
        .json();
    }
}