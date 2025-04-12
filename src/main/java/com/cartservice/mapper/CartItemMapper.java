package com.cartservice.mapper;

import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.entity.CartItemEntity;

public class CartItemMapper {

    public static CartItemResponseDto toResponseDto(CartItemEntity entity) {
        return CartItemResponseDto.builder()
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .build();
    }

}
