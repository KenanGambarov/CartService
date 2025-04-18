package com.cartservice.repository;

import com.cartservice.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemEntity,Long> {


    List<CartItemEntity> findByCartId(Long cartId);

    void deleteAllByProductId(Long productId);
}
