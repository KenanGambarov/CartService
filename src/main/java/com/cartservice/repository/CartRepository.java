package com.cartservice.repository;

import com.cartservice.dto.enums.CartStatus;
import com.cartservice.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<CartEntity,Long> {

    CartEntity findByUserIdAndStatus(Long userId, CartStatus status);

}
