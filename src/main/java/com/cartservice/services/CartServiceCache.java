package com.cartservice.services;

import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;

import java.util.List;
import java.util.Optional;

public interface CartServiceCache {

    Optional<CartEntity> getActiveCartForUser(Long userId);

    Optional<List<CartItemEntity>> getCartItemsFromCacheOrDB(Long cartId);

    void clearCartCache(Long userId, Long cartId);

}
