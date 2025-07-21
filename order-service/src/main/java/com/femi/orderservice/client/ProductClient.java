package com.femi.orderservice.client;

import com.femi.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service", url = "http://localhost:8081")
public interface ProductClient {
    @GetMapping("/api/products/{productId}")
    ProductDTO getProductById(@PathVariable Long productId,
                              @RequestHeader("Authorization") String token);
}
