package com.femi.orderservice.controller;

import com.femi.orderservice.dto.OrderRequestDTO;
import com.femi.orderservice.dto.OrderResponse;
import com.femi.orderservice.dto.OrderResponseDTO;
import com.femi.orderservice.dto.OrderStatusUpdateDTO;
import com.femi.orderservice.service.OrderService;
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
}
