package com.femi.orderservice.service;

import com.femi.orderservice.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO initiatePayment(Long orderId, String buyerId);
}
