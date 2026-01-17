package com.example.ecom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WalletTransaction entity for tracking all wallet movements.
 * Types: CREDIT (money in), DEBIT (money out), COMMISSION (platform fee)
 */
@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String description;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order relatedOrder;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.SUCCESS;
        }
    }

    public enum TransactionType {
        CREDIT, // Money added (from sales)
        DEBIT, // Money withdrawn
        COMMISSION, // Platform commission
        REFUND // Order refund
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
