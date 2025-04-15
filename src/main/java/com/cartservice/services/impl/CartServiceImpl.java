package com.cartservice.services.impl;

import com.cartservice.client.ProductServiceClient;
import com.cartservice.dto.product.ProductDto;
import com.cartservice.dto.request.CartItemDto;
import com.cartservice.dto.enums.CartStatus;
import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;
import com.cartservice.exception.NotFoundException;
import com.cartservice.mapper.CartItemMapper;
import com.cartservice.repository.CartItemRepository;
import com.cartservice.repository.CartRepository;
import com.cartservice.services.CartService;
import com.cartservice.services.CartServiceCache;
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
    private final CartServiceCache cartServiceCache;
    private final ProductServiceClient productServiceClient;

    @Transactional
    @Override
    public void addProductToCart(Long userId, CartItemDto cartItemDto) {
        CartEntity cart = cartServiceCache.getActiveCartForUser(userId);
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
        cartServiceCache.clearCartCache(userId,cart.getId());

    }


    @Override
    public void deleteProductFromCart(Long userId, Long productId) {
        CartEntity cart = cartServiceCache.getActiveCartForUser(userId);

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
        cartServiceCache.clearCartCache(userId,cart.getId());
    }

    @Override
    public List<CartItemResponseDto> getProductsFromCart(Long userId) {
        CartEntity cart = cartServiceCache.getActiveCartForUser(userId);
        if (cart == null) {
            log.warn("Cart not found for user {}", userId);
            throw new NotFoundException("Cart not found");
        }

        List<CartItemEntity> existingItem =  cartServiceCache.getCartItemsFromCacheOrDB(cart.getId());

        if (existingItem.isEmpty()) {
            throw new NotFoundException("Products not found in cart");
        }

        return existingItem.stream().map(CartItemMapper::toResponseDto).toList();

    }


    private Optional<CartItemEntity> getCartItems(Long cartId,Long productId) {
        List<CartItemEntity> items = cartServiceCache.getCartItemsFromCacheOrDB(cartId);

        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }


}
