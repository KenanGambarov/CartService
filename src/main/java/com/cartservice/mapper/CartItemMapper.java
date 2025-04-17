package com.cartservice.mapper;

import com.cartservice.dto.request.CartItemRequestDto;
import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.entity.CartEntity;
import com.cartservice.entity.CartItemEntity;

public class CartItemMapper {

    public static CartItemResponseDto toResponseDto(CartItemEntity entity) {
        return CartItemResponseDto.builder()
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .build();
    }

    public static CartItemEntity createCartItem(CartItemRequestDto dto, CartEntity cart) {
        return CartItemEntity.builder()
                .cart(cart)
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .build();
    }

}
