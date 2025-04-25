package com.cartservice.config.queue;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductQueueConfig {

    private final String productQ;
    private final String productDLQ;
    private final String productQExchange;
    private final String productDLQExchange;
    private final String productQKey;
    private final String productDLQKey;

    public ProductQueueConfig(@Value("${rabbitmq.product-service.queue}") String productQ,
                              @Value("${rabbitmq.product-service.dlq}") String productDLQ) {

        this.productQ = productQ;
        this.productDLQ = productDLQ;
        this.productQExchange = productQ + "_EXCHANGE";
        this.productDLQExchange = productDLQ + "_EXCHANGE";
        this.productQKey = productQ + "_KEY";
        this.productDLQKey = productDLQ + "_KEY";
    }

    @Bean
    DirectExchange productDLQExchange() {
        return new DirectExchange(productDLQExchange);
    }

    @Bean
    DirectExchange productQExchange() {
        return new DirectExchange(productQExchange);
    }

    @Bean
    Queue productDLQ() {
        return QueueBuilder.durable(productDLQ).build();
    }

    @Bean
    Queue productQ() {
        return QueueBuilder.durable(productQ)
                .withArgument("x-dead-letter-exchange", productDLQExchange)
                .withArgument("x-dead-letter-routing-key", productDLQKey)
                .build();
    }

    @Bean
    Binding productDLQBinding() {
        return BindingBuilder.bind(productDLQ())
                .to(productDLQExchange()).with(productDLQKey);
    }

    @Bean
    Binding productQBinding() {
        return BindingBuilder.bind(productQ())
                .to(productQExchange()).with(productQKey);
    }

}
