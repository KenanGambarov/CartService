package com.cartservice.services;

import com.cartservice.dto.request.CartItemDto;
import com.cartservice.dto.response.CartItemResponseDto;

import java.util.List;

public interface CartService {

    void addProductToCart(Long userId, CartItemDto cartItemDto);

    void deleteProductFromCart(Long userId, Long productId);

    List<CartItemResponseDto> getProductsFromCart(Long userId);

}
