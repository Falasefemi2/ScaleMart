package com.femi.orderservice.controller;

import com.femi.orderservice.dto.*;
import com.femi.orderservice.service.OrderService;
import com.femi.orderservice.service.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentServiceImpl  paymentService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody OrderRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {

        String buyerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String token = authHeader.replace("Bearer ", "");

        OrderResponse response = orderService.placeOrder(request, Long.parseLong(buyerId), token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUYER', 'SELLER')")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        String currentUserId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();

        return ResponseEntity.ok(orderService.getOrderById(id, currentUserId, currentUserRole));
    }


    @GetMapping("/buyer/my-orders")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForBuyer(Authentication authentication) {
        Long buyerId = Long.valueOf((String) authentication.getPrincipal());
        return ResponseEntity.ok(orderService.getOrdersForBuyer(buyerId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateDTO statusUpdateDTO
    ) {
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(id, statusUpdateDTO.getOrderStatus());
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{orderId}/pay")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<PaymentResponseDTO> payForOrder(@PathVariable Long orderId) {
        String buyerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(paymentService.initiatePayment(orderId, buyerId));
    }
}
