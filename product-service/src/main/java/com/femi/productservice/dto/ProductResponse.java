package com.femi.productservice.dto;

import com.femi.productservice.model.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long sellerId;
    private List<String> imageUrls;
    private String category;
    private ProductStatus status;
    private LocalDateTime createdAt;
}
