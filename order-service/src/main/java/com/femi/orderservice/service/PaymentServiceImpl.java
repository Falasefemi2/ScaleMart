package com.femi.orderservice.service;

import com.femi.orderservice.dto.PaymentResponseDTO;
import com.femi.orderservice.model.Order;
import com.femi.orderservice.model.PaymentStatus;
import com.femi.orderservice.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.model.checkout.Session;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final OrderRepository orderRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public PaymentResponseDTO initiatePayment(Long orderId, String buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getBuyerId().toString().equals(buyerId)) {
            throw new AccessDeniedException("You are not authorized to pay for this order.");
        }

        BigDecimal unitPrice = order.getTotalAmount()
                .divide(BigDecimal.valueOf(order.getQuantity()), RoundingMode.HALF_UP);
        Long unitPriceInCents = unitPrice.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(order.getProductName())
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(unitPriceInCents)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setPriceData(priceData)
                        .setQuantity(Long.valueOf(order.getQuantity()))
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/success") //frontend success page
                .setCancelUrl("http://localhost:3000/cancel")    //frontend cancel page
                .addLineItem(lineItem)
                .build();

        try {
            Session session = Session.create(params);
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);

            return new PaymentResponseDTO(session.getUrl(), "PENDING");

        } catch (StripeException e) {
            throw new RuntimeException("Stripe payment session creation failed", e);
        }
    }

}
