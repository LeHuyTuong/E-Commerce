package com.example.ecom.service.impl;

import com.example.ecom.exceptions.APIException;
import com.example.ecom.exceptions.ResourceNotFoundException;
import com.example.ecom.model.*;
import com.example.ecom.payload.OrderDTO;
import com.example.ecom.payload.OrderItemDTO;
import com.example.ecom.repositories.OrderItemRepository;
import com.example.ecom.repositories.OrderRepository;
import com.example.ecom.repositories.PaymentRepository;
import com.example.ecom.repositories.ProductRepository;
import com.example.ecom.service.AddressService;
import com.example.ecom.service.CartService;
import com.example.ecom.service.OrderService;
import com.example.ecom.service.WalletService;
import com.example.ecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final AuthUtil authUtil;
    private final CartService cartService;
    private final AddressService addressService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final WalletService walletService;

    @Override
    @Transactional
    public List<OrderDTO> placeOrder(String paymentMethod, Long addressId, String pgPaymentId, String pgStatus,
            String pgResponseMessage, String pgName) {
        String email = authUtil.loggedInEmail();
        Cart cart = cartService.getCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "User Email", email);
        }

        Address address = addressService.findAddressById(addressId);
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty. Cannot place order.");
        }

        // Group items by Seller (User)
        // Note: product.getUser() is the seller
        Map<User, List<CartItem>> itemsBySeller = cartItems.stream()
                .collect(Collectors.groupingBy(item -> {
                    User seller = item.getProduct().getUser();
                    return seller != null ? seller : new User(); // Handle safely, though seller should exist
                }));

        List<OrderDTO> orderDTOs = new ArrayList<>();

        for (Map.Entry<User, List<CartItem>> entry : itemsBySeller.entrySet()) {
            User seller = entry.getKey();
            List<CartItem> sellerItems = entry.getValue();

            // Skip invalid seller if necessary, or assign to admin logic here
            // Assuming valid sellers for now

            // Calculate sub-total for this seller's order
            BigDecimal subTotal = BigDecimal.ZERO;
            for (CartItem item : sellerItems) {
                // Check stock first
                if (item.getProduct().getQuantity() < item.getQuantity()) {
                    throw new APIException("Product " + item.getProduct().getProductName() + " is out of stock");
                }

                BigDecimal price = item.getProductPrice() != null ? item.getProductPrice()
                        : item.getProduct().getSpecialPrice();

                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                subTotal = subTotal.add(itemTotal);
            }

            // Create Order
            Order order = new Order();
            order.setEmail(email);
            order.setOrderDate(LocalDate.now());
            order.setTotalAmount(subTotal);
            order.setOrderStatus("Order Accepted !");
            order.setAddress(address);
            if (seller.getUserId() != null) {
                order.setSeller(seller);
            }

            // Create Payment (OneToOne)
            Payment payment = Payment.builder()
                    .pgPaymentId(pgPaymentId)
                    .pgStatus(pgStatus)
                    .pgName(pgName)
                    .pgResponseMessage(pgResponseMessage)
                    .paymentMethod(paymentMethod)
                    .build();
            payment.setOrder(order);
            payment = paymentRepository.save(payment);
            order.setPayment(payment);

            // BANKING LOGIC: Debit Wallet if payment method is WALLET
            // Must save order FIRST to get orderId
            Order savedOrder = orderRepository.save(order);

            if ("WALLET".equalsIgnoreCase(paymentMethod)) {
                walletService.debitWallet(authUtil.loggedInUser().getUserId(),
                        subTotal, "Payment for Order #" + savedOrder.getOrderId());
                payment.setPgStatus("SUCCESS");
                payment.setPgResponseMessage("Wallet Payment Successful");
                paymentRepository.save(payment); // Update payment status
            }

            // Create OrderItems
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : sellerItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setDiscount(cartItem.getDiscount());

                BigDecimal price = cartItem.getProductPrice() != null ? cartItem.getProductPrice()
                        : cartItem.getProduct().getSpecialPrice();

                BigDecimal orderedPrice = price
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                        .setScale(2, RoundingMode.HALF_UP);

                orderItem.setOrderedProductPrice(orderedPrice);
                orderItems.add(orderItem);

                // Update product stock
                Product product = cartItem.getProduct();
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            }
            orderItems = orderItemRepository.saveAll(orderItems);

            // Map to DTO
            OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
            orderItems.forEach(item -> orderDTO.getOrderItems()
                    .add(modelMapper.map(item, OrderItemDTO.class)));
            orderDTO.setAddressId(addressId);
            orderDTOs.add(orderDTO);
        }

        // Clear cart - copy product IDs first to avoid ConcurrentModificationException
        List<Long> productIds = cartItems.stream()
                .map(item -> item.getProduct().getProductId())
                .collect(Collectors.toList());
        for (Long productId : productIds) {
            cartService.deleteProductFromCart(cart.getCartId(), productId);
        }

        return orderDTOs;
    }

    @Override
    public List<OrderDTO> getUserOrders() {
        String email = authUtil.loggedInEmail();
        List<Order> orders = orderRepository.findByEmailOrderByOrderDateDesc(email);
        return mapOrdersToDTOs(orders);
    }

    @Override
    public List<OrderDTO> getSellerOrders() {
        User seller = authUtil.loggedInUser();
        List<Order> orders = orderRepository.findBySellerOrderByOrderDateDesc(seller);
        return mapOrdersToDTOs(orders);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        if (order.getOrderStatus().equals("Delivered")) {
            throw new APIException("Order is already delivered.");
        }

        order.setOrderStatus(status);

        // If Delivered, trigger wallet processing
        if ("Delivered".equalsIgnoreCase(status)) {
            walletService.processOrderCompletion(order, null);
        }

        Order savedOrder = orderRepository.save(order);

        // Map to DTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        List<OrderItem> orderItems = orderItemRepository.findByOrder(savedOrder);
        orderItems.forEach(item -> orderDTO.getOrderItems()
                .add(modelMapper.map(item, OrderItemDTO.class)));
        orderDTO.setAddressId(savedOrder.getAddress().getAddressId());
        return orderDTO;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        return mapOrdersToDTOs(orders);
    }

    private List<OrderDTO> mapOrdersToDTOs(List<Order> orders) {
        List<OrderDTO> orderDTOs = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            for (OrderItem item : orderItems) {
                OrderItemDTO itemDTO = modelMapper.map(item, OrderItemDTO.class);
                orderDTO.getOrderItems().add(itemDTO);
            }
            orderDTOs.add(orderDTO);
        }
        return orderDTOs;
    }
}
