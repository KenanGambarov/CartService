package com.cartservice.services;

import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;

import java.util.List;

public interface CartServiceCache {

    CartEntity getActiveCartForUser(Long userId);

    List<CartItemEntity> getCartItemsFromCacheOrDB(Long cartId);

    void clearCartCache(Long userId, Long cartId);
}
