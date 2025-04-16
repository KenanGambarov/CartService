package com.cartservice.client.decoder;

import lombok.Getter;

@Getter
public enum JsonNodeFieldName {

    MESSAGE("message");

    private final String value;

     JsonNodeFieldName(String value) {
        this.value = value;
    }

}
