package com.cartservice.config.queue;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderQueueConfig {

    private final String orderQ;
    private final String orderDLQ;
    private final String orderQExchange;
    private final String orderDLQExchange;
    private final String orderQKey;
    private final String orderDLQKey;

    public OrderQueueConfig(@Value("${rabbitmq.order-service.queue}") String orderQ,
                            @Value("${rabbitmq.order-service.dlq}") String orderDLQ) {

        this.orderQ = orderQ;
        this.orderDLQ = orderDLQ;
        this.orderQExchange = orderQ + "_EXCHANGE";
        this.orderDLQExchange = orderDLQ + "_EXCHANGE";
        this.orderQKey = orderQ + "_KEY";
        this.orderDLQKey = orderDLQ + "_KEY";
    }

    @Bean
    DirectExchange orderDLQExchange() {
        return new DirectExchange(orderDLQExchange);
    }

    @Bean
    DirectExchange orderQExchange() {
        return new DirectExchange(orderQExchange);
    }

    @Bean
    Queue orderDLQ() {
        return QueueBuilder.durable(orderDLQ).build();
    }

    @Bean
    Queue orderQ() {
        return QueueBuilder.durable(orderQ)
                .withArgument("x-dead-letter-exchange", orderDLQExchange)
                .withArgument("x-dead-letter-routing-key", orderDLQKey)
                .build();
    }

    @Bean
    Binding orderDLQBinding() {
        return BindingBuilder.bind(orderDLQ())
                .to(orderDLQExchange()).with(orderDLQKey);
    }

    @Bean
    Binding orderQBinding() {
        return BindingBuilder.bind(orderQ())
                .to(orderQExchange()).with(orderQKey);
    }

}
