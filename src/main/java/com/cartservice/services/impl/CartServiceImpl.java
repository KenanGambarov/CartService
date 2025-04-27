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
        Optional<CartEntity> cartOptional = cartServiceCache.getActiveCartForUser(userId);
        CartEntity cart = cartOptional.get();
        if (cartOptional.isEmpty()) {
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
        Optional<CartEntity> cartOptional  = cartServiceCache.getActiveCartForUser(userId);
        CartEntity cart = cartOptional.get();
        if (cartOptional.isEmpty()) {
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
    public void changeCartStatus(OrderRequestDto orderRequestDto) {
        log.info("cart status1 {}",CartStatus.ORDERED);
        Optional<CartEntity> cartOptional = cartServiceCache.getActiveCartForUser(orderRequestDto.getUserId());

        if (cartOptional.isEmpty()) {
            throw new NotFoundException(ExceptionConstants.CART_NOT_FOUND.getMessage());
        }
        CartEntity cart = cartOptional.get();
        log.info("cart status2 {}",CartStatus.ORDERED);
        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);
        cartServiceCache.clearCartCache(orderRequestDto.getUserId(),cart.getId());
    }

    @Override
    public List<CartItemResponseDto> getProductsFromCart(Long userId) {
        Optional<CartEntity> cartOptional = cartServiceCache.getActiveCartForUser(userId);
        if (cartOptional.isEmpty()) {
            log.warn("Cart not found for user {}", userId);
            throw new NotFoundException(ExceptionConstants.CART_NOT_FOUND.getMessage());
        }
        CartEntity cart = cartOptional.get();
        Optional<List<CartItemEntity>> existingItemOptional =  cartServiceCache.getCartItemsFromCacheOrDB(cart.getId());

        if (existingItemOptional.isEmpty()) {
            throw new NotFoundException("Products not found in cart");
        }
        List<CartItemEntity> existingItem = existingItemOptional.get();
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
        List<CartItemEntity> items = cartServiceCache.getCartItemsFromCacheOrDB(cartId).orElseThrow(()-> new NotFoundException("Products not found in cart"));
        return items.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
    }


}
