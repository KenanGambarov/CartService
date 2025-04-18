package com.cartservice.dto.enums;

import lombok.Getter;

@Getter
public enum RabbitQueueType {

    QUEUE_NAME("PRODUCT_UPDATE");

    private final String queueName;

    RabbitQueueType(String queueName){
        this.queueName = queueName;
    }

}
