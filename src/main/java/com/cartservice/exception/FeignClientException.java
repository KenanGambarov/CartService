package com.cartservice.exception;

import lombok.Getter;

@Getter
public class FeignClientException extends RuntimeException {

    private final int status;

    public FeignClientException(final String message,final int status) {
        super(message);
        this.status = status;
    }
}
