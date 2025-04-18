package com.cartservice.services.impl;

import com.cartservice.client.ProductServiceClient;
import com.cartservice.dto.product.ProductDto;
import com.cartservice.dto.request.CartItemRequestDto;
import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.dto.response.StockRequestDto;
import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;
import com.cartservice.exception.ExceptionConstants;
import com.cartservice.exception.NotFoundException;
import com.cartservice.mapper.CartItemMapper;
import com.cartservice.mapper.CartMapper;
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
    public void addProductToCart(Long userId, CartItemRequestDto cartItemDto) {
        CartEntity cart = cartServiceCache.getActiveCartForUser(userId);
        if (cart == null) {
            cart = CartMapper.createCart(userId);
            cart = cartRepository.save(cart);
            log.info("New cart created for user {}", userId);

        }
        ProductDto product = productServiceClient.getProductById(cartItemDto.getProductId());
        log.info("Feign client product {} ", product);

        Optional<CartItemEntity> existingItemOpt =
                getCartItems(cart.getId(), cartItemDto.getProductId());

        if (existingItemOpt.isPresent()) {
            CartItemEntity existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + cartItemDto.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItemEntity cartItem = CartItemMapper.createCartItem(cartItemDto,cart);
            cartItemRepository.save(cartItem);
            log.info("User {} is adding product {} (quantity: {}) to cart", userId, cartItemDto.getProductId(), cartItemDto.getQuantity());

        }
        cartServiceCache.clearCartCache(userId,cart.getId());

    }


    @Override
    public void deleteProductFromCart(Long userId, Long productId) {
        CartEntity cart = cartServiceCache.getActiveCartForUser(userId);

        if (cart == null) {
            throw new NotFoundException(ExceptionConstants.CART_NOT_FOUND.getMessage());
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
            throw new NotFoundException(ExceptionConstants.CART_NOT_FOUND.getMessage());
        }

        List<CartItemEntity> existingItem =  cartServiceCache.getCartItemsFromCacheOrDB(cart.getId());

        if (existingItem.isEmpty()) {
            throw new NotFoundException("Products not found in cart");
        }

        return existingItem.stream().map(CartItemMapper::toResponseDto).toList();

    }

    @Override
    public void removeOutOfStockItems(StockRequestDto requestDto) {
        if(requestDto.getQuantity()==0){
            cartItemRepository.deleteAllByProductId(requestDto.getProductId());
            log.info("Out of stock items removed");
        }


    }


    private Optional<CartItemEntity> getCartItems(Long cartId,Long productId) {
        List<CartItemEntity> items = cartServiceCache.getCartItemsFromCacheOrDB(cartId);

        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }


}
