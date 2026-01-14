package com.example.ecom.controller;

import com.example.ecom.payload.OrderDTO;
import com.example.ecom.payload.OrderRequestDTO;
import com.example.ecom.payload.StripePaymentDto;
import com.example.ecom.security.response.MessageResponse;
import com.example.ecom.service.OrderService;
import com.example.ecom.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StripeService stripeService;

    @PostMapping("/users/payments/{paymentMethod}")
    public ResponseEntity<List<OrderDTO>> orderProduct(@PathVariable String paymentMethod,
            @RequestBody OrderRequestDTO orderRequestDTO) {
        List<OrderDTO> orderDTOs = orderService.placeOrder(
                paymentMethod,
                orderRequestDTO.getAddressId(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage(),
                orderRequestDTO.getPgName());

        return new ResponseEntity<>(orderDTOs, HttpStatus.CREATED);
    }

    @PostMapping("/stripe/create-session")
    public ResponseEntity<?> createStripeSession(@RequestBody StripePaymentDto stripePaymentDto) {
        try {
            Session session = stripeService.createCheckoutSession(stripePaymentDto);
            // Return URL for Frontend to redirect
            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Unable to create payment session: " + e.getMessage()));
        }
    }

    @GetMapping("/stripe/validate")
    public ResponseEntity<?> validateStripeSession(@RequestParam String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            if ("paid".equals(session.getPaymentStatus())) {
                // Payment success, create order
                Map<String, String> metadata = session.getMetadata();
                Long addressId = Long.parseLong(metadata.get("addressId"));

                // Assuming paymentMethod is STRIPE and getting other details from session if
                // needed
                List<OrderDTO> orderDTOs = orderService.placeOrder(
                        "STRIPE",
                        addressId,
                        session.getPaymentIntent(), // Use PaymentIntent ID as pgPaymentId
                        session.getPaymentStatus(),
                        "Payment Successful",
                        "STRIPE");

                return new ResponseEntity<>(orderDTOs, HttpStatus.CREATED);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Payment not successful. Status: " + session.getPaymentStatus()));
            }
        } catch (StripeException e) {
            log.error("Stripe validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Validation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/users/orders")
    public ResponseEntity<?> getUserOrders() {
        List<OrderDTO> orders = orderService.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    // NEW: Get seller orders
    @GetMapping("/seller/orders")
    public ResponseEntity<?> getSellerOrders() {
        List<OrderDTO> orders = orderService.getSellerOrders();
        return ResponseEntity.ok(orders);
    }

    // NEW: Update order status (Seller/Admin)
    @PutMapping("/{orderId}/status/{status}")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @PathVariable String status) {
        OrderDTO orderDTO = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(orderDTO);
    }

    // NEW: Admin - Get all orders
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}
