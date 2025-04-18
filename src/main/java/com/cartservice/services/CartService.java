package com.cartservice.services;

import com.cartservice.dto.request.CartItemRequestDto;
import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.dto.response.StockRequestDto;

import java.util.List;

public interface CartService {

    void addProductToCart(Long userId, CartItemRequestDto cartItemDto);

    void deleteProductFromCart(Long userId, Long productId);

    List<CartItemResponseDto> getProductsFromCart(Long userId);

    void removeOutOfStockItems(StockRequestDto requestDto);

}
