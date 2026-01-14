package com.example.ecom.controller;

import com.example.ecom.payload.WalletDTO;
import com.example.ecom.payload.WalletTransactionDTO;
import com.example.ecom.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Get current user's wallet
     */
    @GetMapping
    public ResponseEntity<WalletDTO> getMyWallet() {
        WalletDTO wallet = walletService.getMyWallet();
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get current user's transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionDTO>> getMyTransactions() {
        List<WalletTransactionDTO> transactions = walletService.getMyTransactionHistory();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get wallet by user ID (Admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletDTO> getWalletByUserId(@PathVariable Long userId) {
        WalletDTO wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get platform commission wallet (Admin only)
     */
    @GetMapping("/platform")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WalletDTO> getPlatformWallet() {
        WalletDTO wallet = walletService.getPlatformWallet();
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get transaction history for specific user (Admin only)
     */
    @GetMapping("/user/{userId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WalletTransactionDTO>> getUserTransactions(@PathVariable Long userId) {
        List<WalletTransactionDTO> transactions = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }
}
