package com.example.iot2kafka;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * Iot to Kafka router.
 */
public class IotToKafkaRoute extends RouteBuilder {

    /**
     * The REST DSL is configured to utilise the Netty HTTP.
     * The payload is produced into Kafka topic.
     */
    public void configure() {
        restConfiguration()
            .component("netty-http")
            .host("0.0.0.0")
            .port("12345")
            .bindingMode(RestBindingMode.auto);

        rest("/api/v1")
            .post("/location")
            .to("kafka:test?brokers=data-toolbox-kafka-1:9092");
    }
}
