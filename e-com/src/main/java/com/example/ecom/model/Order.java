package com.example.ecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Email
    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "order", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDate orderDate;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String orderStatus;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    // Seller who owns the products in this order (for multi-seller orders, this is
    // primary seller)
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // Commission amount taken by platform
    @Column(precision = 19, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    // Seller's earning after commission
    @Column(precision = 19, scale = 2)
    private BigDecimal sellerEarning = BigDecimal.ZERO;
}
