package com.femi.orderservice.service;

import com.femi.orderservice.dto.OrderRequestDTO;
import com.femi.orderservice.dto.OrderResponse;
import com.femi.orderservice.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequestDTO request, Long buyerId, String token);
    OrderResponseDTO getOrderById(Long orderId, String userId, String role);
    List<OrderResponseDTO> getOrdersForBuyer(Long buyerId);
    OrderResponseDTO updateOrderStatus(Long orderId, String status);
}
