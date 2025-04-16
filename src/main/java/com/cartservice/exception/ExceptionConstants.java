package com.cartservice.exception;

import lombok.Getter;

@Getter
public enum ExceptionConstants {

    VALIDATION_FAILED("Validation Failed"),
    UNEXPECTED_ERROR("Unexpected Error"),
    CLIENT_ERROR("Exception from Client"),
    PRODUCT_NOT_FOUND("Product Not Found"),
    CART_NOT_FOUND("Cart Not Found"),
    PRODUCT_NOT_FOUND_IN_CART("Product Not Found in Cart");

    private final String message;

    ExceptionConstants(String message) {
        this.message = message;
    }
}
