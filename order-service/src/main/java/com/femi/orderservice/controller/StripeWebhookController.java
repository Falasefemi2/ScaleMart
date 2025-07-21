package com.femi.orderservice.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,@RequestHeader("Stripe-Signature") String sigHeader) {

        String endpointSecret = "whsec_...";

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            String sessionId = session.getId();
            String clientReferenceId = session.getClientReferenceId(); // Optional: orderId

            // TODO: Lookup order by session or clientReferenceId
            // order.setPaymentStatus(PAID)
            // orderRepository.save(order)
        }

        return ResponseEntity.ok("Webhook received");
    }
}
