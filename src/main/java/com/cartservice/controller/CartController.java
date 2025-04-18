package com.cartservice.controller;

import com.cartservice.dto.request.CartItemRequestDto;
import com.cartservice.dto.response.CartItemResponseDto;
import com.cartservice.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("/{userId}/product/")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProductToCart(@PathVariable Long userId, @Validated @RequestBody CartItemRequestDto cartItemDto){
        cartService.addProductToCart(userId,cartItemDto);
    }

    @DeleteMapping("/{userId}/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long userId, @PathVariable Long productId){
        cartService.deleteProductFromCart(userId,productId);
    }

    @GetMapping("/{userId}/products")
    @ResponseStatus(HttpStatus.OK)
    public List<CartItemResponseDto> getProducts(@PathVariable Long userId){
        return cartService.getProductsFromCart(userId);
    }

}
