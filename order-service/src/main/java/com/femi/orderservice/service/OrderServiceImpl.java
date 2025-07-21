package com.femi.orderservice.service;

import com.femi.orderservice.client.ProductClient;
import com.femi.orderservice.dto.OrderRequestDTO;
import com.femi.orderservice.dto.OrderResponse;
import com.femi.orderservice.dto.OrderResponseDTO;
import com.femi.orderservice.dto.ProductDTO;
import com.femi.orderservice.model.Order;
import com.femi.orderservice.model.OrderStatus;
import com.femi.orderservice.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;


    @Transactional
    public OrderResponse placeOrder(OrderRequestDTO request, Long buyerId, String token) {
        ProductDTO product = productClient.getProductById(request.getProductId(), "Bearer " + token);
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .productId(product.getId())
                .buyerId(buyerId)
                .sellerId(product.getSellerId())
                .quantity(request.getQuantity())
                .totalAmount(totalPrice)
                .productName(product.getName())
                .orderStatus(OrderStatus.PLACED)
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);

        return OrderResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalAmount())
                .status(order.getOrderStatus())
                .build();
    }

    public OrderResponseDTO getOrderById(Long orderId, String userId, String role) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));

    // Role-based access check
    if (role.equals("ROLE_ADMIN")) {
        return mapToDTO(order);
    }

    if (role.equals("ROLE_BUYER") && !order.getBuyerId().toString().equals(userId)) {
        throw new AccessDeniedException("You can only view your own orders.");
    }

    if (role.equals("ROLE_SELLER") && !order.getSellerId().toString().equals(userId)) {
        throw new AccessDeniedException("You can only view orders that belong to you.");
    }

    return mapToDTO(order);
}

    @Override
    public List<OrderResponseDTO> getOrdersForBuyer(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Order status cannot be null or empty");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        order.setOrderStatus(OrderStatus.valueOf(status.toUpperCase()));
        orderRepository.save(order);

        return mapToDTO(order);
    }

    private OrderResponseDTO mapToDTO(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .productName(order.getProductName())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
