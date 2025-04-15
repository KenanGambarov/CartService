package com.cartservice.mapper;

import com.cartservice.dto.enums.CartStatus;
import com.cartservice.entity.CartEntity;

public class CartMapper {

    public static CartEntity createCart(Long userId){
        return CartEntity.builder()
                .userId(userId)
                .status(CartStatus.ACTIVE)
                .build();
    }
}
