package com.example.ecom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet entity for storing user balance.
 * Used by Sellers to track earnings and by Admin for commission.
 */
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
