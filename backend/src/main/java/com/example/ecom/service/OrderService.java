package com.example.ecom.service;

import com.example.ecom.payload.OrderDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrderService {
    @Transactional
    public List<OrderDTO> placeOrder(String paymentMethod, Long addressId, String pgPaymentId, String pgStatus,
            String pgResponseMessage, String pgName);

    List<OrderDTO> getUserOrders();

    List<OrderDTO> getSellerOrders();

    @Transactional
    OrderDTO updateOrderStatus(Long orderId, String status);

    // For Admin: get all orders
    List<OrderDTO> getAllOrders();
}
