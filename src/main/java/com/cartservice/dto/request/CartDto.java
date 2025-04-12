package com.cartservice.dto.request;

import com.cartservice.dto.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto implements Serializable {

    private CartStatus status;

    private Date createdAt;

}
