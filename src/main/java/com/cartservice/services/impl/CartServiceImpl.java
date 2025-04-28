package com.cartservice.services.impl;

import com.cartservice.client.ProductServiceClient;
import com.cartservice.dto.enums.CartStatus;
import com.cartservice.dto.product.ProductDto;
import com.cartservice.dto.request.CartItemRequestDto;
import com.cartservice.dto.request.queue.OrderRequestDto;
import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.dto.request.queue.StockRequestDto;
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

import java.util.Collections;
import java.util.List;

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
        CartEntity cart = cartServiceCache.getActiveCartForUser(userId)
                .orElseGet(() -> cartRepository.save(CartMapper.createCart(userId)));

        ProductDto product = productServiceClient.getProductById(cartItemDto.getProductId());
        log.info("Feign client product {} ", product);

        CartItemEntity existingItem = findExistingCartItem(cart.getId(), cartItemDto.getProductId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + cartItemDto.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            cartItemRepository.save(CartItemMapper.createCartItem(cartItemDto,cart));
            log.info("User {} is adding product {} (quantity: {}) to cart", userId, cartItemDto.getProductId(), cartItemDto.getQuantity());

        }

        cartServiceCache.clearCartCache(userId,cart.getId());

    }


    @Override
    public void deleteProductFromCart(Long userId, Long productId) {
        CartEntity cart  = getActiveCartForUser(userId);

        CartItemEntity existingItem =
                getCartItems(cart.getId(), productId);
        if (existingItem.getQuantity() == 1) {
            cartItemRepository.delete(existingItem);
        } else {
            existingItem.setQuantity(existingItem.getQuantity()-1);
            cartItemRepository.save(existingItem);
        }
        cartServiceCache.clearCartCache(userId,cart.getId());
    }

    @Override
    public void changeCartStatus(OrderRequestDto orderRequestDto) {
        CartEntity cart = getActiveCartForUser(orderRequestDto.getUserId());
        log.info("cart status2 {}",CartStatus.ORDERED);
        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);
        cartServiceCache.clearCartCache(orderRequestDto.getUserId(),cart.getId());
    }

    @Override
    public List<CartItemResponseDto> getProductsFromCart(Long userId) {
        CartEntity cart = getActiveCartForUser(userId);
        List<CartItemEntity> existingItem =  cartServiceCache.getCartItemsFromCacheOrDB(cart.getId())
                .orElseThrow(() -> new NotFoundException(ExceptionConstants.PRODUCT_NOT_FOUND_IN_CART.getMessage()));
        return existingItem.stream().map(CartItemMapper::toResponseDto).toList();

    }

    @Override
    public void removeOutOfStockItems(StockRequestDto requestDto) {
        if(requestDto.getQuantity()==0){
            cartItemRepository.deleteAllByProductId(requestDto.getProductId());
            log.info("Out of stock items removed");
        }


    }

    private CartEntity getActiveCartForUser(Long userId) {
        return cartServiceCache.getActiveCartForUser(userId)
                .orElseThrow(() -> new NotFoundException(ExceptionConstants.CART_NOT_FOUND.getMessage()));
    }


    private CartItemEntity getCartItems(Long cartId,Long productId) {
        return cartServiceCache.getCartItemsFromCacheOrDB(cartId)
                .flatMap(items -> items.stream()
                        .filter(item -> item.getProductId().equals(productId))
                        .findFirst())
                .orElseThrow(()-> new NotFoundException(ExceptionConstants.PRODUCT_NOT_FOUND_IN_CART.getMessage()));
    }

    private List<CartItemEntity> getCartItemsForCart(Long cartId) {
        return cartServiceCache.getCartItemsFromCacheOrDB(cartId)
                .orElseGet(Collections::emptyList);
    }

    private CartItemEntity findExistingCartItem(Long cartId, Long productId) {
        return getCartItemsForCart(cartId).stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst().orElse(null);
    }


}
