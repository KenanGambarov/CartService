package com.cartservice.client;

import com.cartservice.dto.product.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${client.product-service.url}")
public interface ProductServiceClient {


    @GetMapping("/v1/product/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);

}
