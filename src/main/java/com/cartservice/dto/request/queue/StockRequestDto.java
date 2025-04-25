package com.cartservice.dto.request.queue;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDto {

    @NotNull
    private Long productId;

    @Positive
    private Integer quantity;

}
