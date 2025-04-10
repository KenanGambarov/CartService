package com.cartservice.services.impl;

import com.cartservice.client.ProductServiceClient;
import com.cartservice.dto.product.ProductDto;
import com.cartservice.dto.request.CartItemDto;
import com.cartservice.dto.enums.CartStatus;
import com.cartservice.dto.responso.CartItemResponseDto;
import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;
import com.cartservice.exception.NotFoundException;
import com.cartservice.mapper.CartItemMapper;
import com.cartservice.repository.CartItemRepository;
import com.cartservice.repository.CartRepository;
import com.cartservice.services.CartService;
import com.cartservice.util.CacheUtil;
import com.cartservice.util.constraints.CartCacheConstraints;
import com.cartservice.util.constraints.CartCacheDurationConstraints;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CacheUtil cacheUtil;
    private final ProductServiceClient productServiceClient;

    @Transactional
    @Override
    public void addProductToCart(Long userId, CartItemDto cartItemDto) {
        CartEntity cart = getActiveCartForUser(userId);
        log.info("userId {} ", userId);
        if (cart == null) {
            cart = CartEntity.builder()
                    .userId(userId)
                    .status(CartStatus.ACTIVE)
                    .build();
            cart = cartRepository.save(cart);
            log.info("New cart created for user {}", userId);

        }
        log.info("getProductId {} ", cartItemDto.getProductId());
        ProductDto product = productServiceClient.getProductById(cartItemDto.getProductId());

        log.info("Feign client product {} ", product);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        Optional<CartItemEntity> existingItemOpt =
                getCartItems(cart.getId(), cartItemDto.getProductId());

        if (existingItemOpt.isPresent()) {
            CartItemEntity existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + cartItemDto.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItemEntity cartItem = CartItemEntity.builder()
                    .cart(cart)
                    .productId(cartItemDto.getProductId())
                    .quantity(cartItemDto.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
            log.info("User {} is adding product {} (quantity: {}) to cart", userId, cartItemDto.getProductId(), cartItemDto.getQuantity());

        }
        clearCartCache(userId,cart.getId());

    }


    @Override
    public void deleteProductFromCart(Long userId, Long productId) {
        CartEntity cart = getActiveCartForUser(userId);

        if (cart == null) {
            throw new NotFoundException("Cart not found for user");
        }

        Optional<CartItemEntity> existingItemOpt =
                getCartItems(cart.getId(), productId);

        if (existingItemOpt.isEmpty()) {
            throw new NotFoundException("Product not found in cart");
        }
        CartItemEntity existingItem = existingItemOpt.get();

        if (existingItem.getQuantity() == 1) {
            cartItemRepository.delete(existingItem);
        } else {
            existingItem.setQuantity(existingItem.getQuantity()-1);
            cartItemRepository.save(existingItem);
        }
        clearCartCache(userId,cart.getId());
    }

    @Override
    public List<CartItemResponseDto> getProductsFromCart(Long userId) {
        CartEntity cart = getActiveCartForUser(userId);
        if (cart == null) {
            log.warn("Cart not found for user {}", userId);
            throw new NotFoundException("Cart not found");
        }

        List<CartItemEntity> existingItem =  cacheUtil.getOrLoad(CartCacheConstraints.CART_ITEMS_KEY.getKey(cart.getId()),
                () -> {
                    return cartItemRepository.findByCartId(cart.getId());
                },
                CartCacheDurationConstraints.DAY.toDuration()
        );

        if (existingItem.isEmpty()) {
            throw new NotFoundException("Products not found in cart");
        }

        return existingItem.stream().map(CartItemMapper::toResponseDto).toList();

    }

    private CartEntity getActiveCartForUser(Long userId) {
        return cacheUtil.getOrLoad(CartCacheConstraints.CART_KEY.getKey(userId),
                () -> {
                    return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
                },
                CartCacheDurationConstraints.DAY.toDuration()
        );

    }

    private Optional<CartItemEntity> getCartItems(Long cartId,Long productId) {
        List<CartItemEntity> items = cacheUtil.getOrLoad(CartCacheConstraints.CART_ITEMS_KEY.getKey(cartId),
                () -> cartItemRepository.findByCartId(cartId),
                CartCacheDurationConstraints.DAY.toDuration());

        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }

    private void clearCartCache(Long userId, Long cartId) {
        cacheUtil.deleteFromCache(CartCacheConstraints.CART_KEY.getKey(userId));
        cacheUtil.deleteFromCache(CartCacheConstraints.CART_ITEMS_KEY.getKey(cartId));
        log.debug("Cache cleared for user {} and cart {}", userId, cartId);
    }
}
