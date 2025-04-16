package com.cartservice.services.impl;

import com.cartservice.dto.enums.CartStatus;
import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;
import com.cartservice.repository.CartItemRepository;
import com.cartservice.repository.CartRepository;
import com.cartservice.services.CartServiceCache;
import com.cartservice.util.CacheUtil;
import com.cartservice.util.constraints.CartCacheConstraints;
import com.cartservice.util.constraints.CartCacheDurationConstraints;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class CartServiceCacheImpl implements CartServiceCache {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CacheUtil cacheUtil;

    @Override
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "fallbackGetActiveCartForUser")
    @Retry(name = "redisRetry", fallbackMethod = "fallbackGetActiveCartForUser")
    public CartEntity getActiveCartForUser(Long userId) {
        return cacheUtil.getOrLoad(CartCacheConstraints.CART_KEY.getKey(userId),
                () -> cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE),
                CartCacheDurationConstraints.DAY.toDuration()
        );

    }

    public CartEntity fallbackGetActiveCartForUser(Long userId, Throwable t) {
        log.error("Redis not available for getActiveCartForUser {}, falling back to DB. Error: {}",userId, t.getMessage());
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
    }

    @Override
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "fallbackCartItems")
    @Retry(name = "redisRetry", fallbackMethod = "fallbackCartItems")
    public List<CartItemEntity> getCartItemsFromCacheOrDB(Long cartId) {
        return cacheUtil.getOrLoad(CartCacheConstraints.CART_ITEMS_KEY.getKey(cartId),
                () -> cartItemRepository.findByCartId(cartId),
                CartCacheDurationConstraints.DAY.toDuration());
    }

    public List<CartItemEntity> fallbackCartItems(Long cartId, Throwable t) {
        log.error("Redis not available for getCartItems for cart{}, falling back to DB. Error: {}",cartId, t.getMessage());
        return  cartItemRepository.findByCartId(cartId);
    }

    @Override
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "fallbackClearCartCache")
    @Retry(name = "redisRetry", fallbackMethod = "fallbackClearCartCache")
    public void clearCartCache(Long userId, Long cartId) {
        cacheUtil.deleteFromCache(CartCacheConstraints.CART_KEY.getKey(userId));
        cacheUtil.deleteFromCache(CartCacheConstraints.CART_ITEMS_KEY.getKey(cartId));
        log.debug("Cache cleared for user {} and cart {}", userId, cartId);
    }

    public void fallbackClearCartCache(Long userId, Long cartId, Throwable t) {
        log.warn("Redis not available to clear cache for user {} and cart {}, ignoring. Error: {}", userId, cartId, t.getMessage());
    }
}
