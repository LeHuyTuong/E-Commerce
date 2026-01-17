package com.example.ecom.payload;

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
public class WalletDTO {
    private Long walletId;
    private Long userId;
    private String username;
    private BigDecimal balance;
    private BigDecimal totalEarnings;
    private BigDecimal pendingBalance;
    private LocalDateTime lastUpdated;
}
