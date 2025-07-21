package com.femi.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponseDTO {
    private Long id;
    private Long buyerId;
    private Long productId;
    private String productName;
    private BigDecimal totalAmount;
    private Integer quantity;
    private String orderStatus;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
