package com.cartservice.util.constraints;

public enum CartCacheConstraints {

    CART_KEY("ms-cart:carts:%s"),
    CART_ITEMS_KEY("ms-cart:cart-items:%s");

    private final String keyFormat;

    CartCacheConstraints(String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getKey(Object... args) {
        return String.format(this.keyFormat, args);
    }
}
