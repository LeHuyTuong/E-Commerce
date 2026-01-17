package com.example.ecom.service;

import com.example.ecom.model.Order;
import com.example.ecom.model.Wallet;
import com.example.ecom.model.WalletTransaction;
import com.example.ecom.payload.WalletDTO;
import com.example.ecom.payload.WalletTransactionDTO;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {

    // Get or create wallet for user
    WalletDTO getWalletByUserId(Long userId);

    WalletDTO getMyWallet();

    // Credit money to wallet (from completed sales)
    WalletDTO creditWallet(Long userId, BigDecimal amount, Order order, String description);

    // Debit money from wallet (withdrawal)
    WalletDTO debitWallet(Long userId, BigDecimal amount, String description);

    // Process order completion - split between seller and platform
    void processOrderCompletion(Order order, BigDecimal commissionRate);

    // Get transaction history
    List<WalletTransactionDTO> getTransactionHistory(Long userId);

    List<WalletTransactionDTO> getMyTransactionHistory();

    // Get platform (admin) commission wallet
    WalletDTO getPlatformWallet();
}
