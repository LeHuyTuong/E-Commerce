package com.example.ecom.payload;

import com.example.ecom.model.WalletTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionDTO {
    private Long transactionId;
    private BigDecimal amount;
    private WalletTransaction.TransactionType type;
    private String description;
    private Long orderId;
    private LocalDateTime createdAt;
}
