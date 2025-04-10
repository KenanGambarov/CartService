package com.cartservice.dto.responso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDto implements Serializable {

    private Long productId;

    private Integer quantity;

}
