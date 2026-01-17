package com.example.ecom.service.impl;

import com.example.ecom.exceptions.ResourceNotFoundException;
import com.example.ecom.model.*;
import com.example.ecom.payload.WalletDTO;
import com.example.ecom.payload.WalletTransactionDTO;
import com.example.ecom.repositories.UserRepository;
import com.example.ecom.repositories.WalletRepository;
import com.example.ecom.repositories.WalletTransactionRepository;
import com.example.ecom.service.WalletService;
import com.example.ecom.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.10"); // 10%
    private static final Long PLATFORM_ADMIN_USER_ID = 1L; // Admin user for platform commission

    @Override
    public WalletDTO getWalletByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        Wallet wallet = getOrCreateWallet(user);
        return mapToDTO(wallet);
    }

    @Override
    public WalletDTO getMyWallet() {
        User user = authUtil.loggedInUser();
        Wallet wallet = getOrCreateWallet(user);
        return mapToDTO(wallet);
    }

    @Override
    @Transactional
    public WalletDTO creditWallet(Long userId, BigDecimal amount, Order order, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        Wallet wallet = getOrCreateWallet(user);

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalEarnings(wallet.getTotalEarnings().add(amount));
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(WalletTransaction.TransactionType.CREDIT)
                .description(description)
                .relatedOrder(order)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .build();
        transactionRepository.save(transaction);

        log.info("Wallet Credited: UserId={}, Amount={}, Balance={}", userId, amount, wallet.getBalance());

        return mapToDTO(wallet);
    }

    @Override
    @Transactional
    public WalletDTO debitWallet(Long userId, BigDecimal amount, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        Wallet wallet = getOrCreateWallet(user);

        if (wallet.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient balance for UserId={}. Required={}, Available={}", userId, amount,
                    wallet.getBalance());
            throw new IllegalArgumentException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount.negate())
                .type(WalletTransaction.TransactionType.DEBIT)
                .status(WalletTransaction.TransactionStatus.SUCCESS)
                .description(description)
                .build();
        transactionRepository.save(transaction);

        log.info("Wallet Debited: UserId={}, Amount={}, NewBalance={}", userId, amount, wallet.getBalance());

        return mapToDTO(wallet);
    }

    @Override
    @Transactional
    public void processOrderCompletion(Order order, BigDecimal commissionRate) {
        if (order.getSeller() == null) {
            return; // No seller to pay
        }

        BigDecimal rate = commissionRate != null ? commissionRate : DEFAULT_COMMISSION_RATE;
        BigDecimal totalAmount = order.getTotalAmount();

        // Calculate commission and seller earning
        BigDecimal commission = totalAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sellerEarning = totalAmount.subtract(commission);

        // Credit seller wallet
        creditWallet(order.getSeller().getUserId(), sellerEarning, order,
                "Payment for order #" + order.getOrderId());

        // Credit platform commission to admin wallet
        creditPlatformCommission(commission, order);
    }

    private void creditPlatformCommission(BigDecimal amount, Order order) {
        User admin = userRepository.findById(PLATFORM_ADMIN_USER_ID)
                .orElse(null);
        if (admin == null) {
            return; // No admin user configured
        }

        Wallet wallet = getOrCreateWallet(admin);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalEarnings(wallet.getTotalEarnings().add(amount));
        walletRepository.save(wallet);

        // Create commission transaction
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .amount(amount)
                .type(WalletTransaction.TransactionType.COMMISSION)
                .description("Commission from order #" + order.getOrderId())
                .relatedOrder(order)
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    public List<WalletTransactionDTO> getTransactionHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        Wallet wallet = getOrCreateWallet(user);

        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet)
                .stream()
                .map(this::mapToTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<WalletTransactionDTO> getMyTransactionHistory() {
        User user = authUtil.loggedInUser();
        Wallet wallet = getOrCreateWallet(user);

        return transactionRepository.findByWalletOrderByCreatedAtDesc(wallet)
                .stream()
                .map(this::mapToTransactionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WalletDTO getPlatformWallet() {
        User admin = userRepository.findById(PLATFORM_ADMIN_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "userId", PLATFORM_ADMIN_USER_ID));
        Wallet wallet = getOrCreateWallet(admin);
        return mapToDTO(wallet);
    }

    private Wallet getOrCreateWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setTotalEarnings(BigDecimal.ZERO);
                    newWallet.setPendingBalance(BigDecimal.ZERO);
                    return walletRepository.save(newWallet);
                });
    }

    private WalletDTO mapToDTO(Wallet wallet) {
        return WalletDTO.builder()
                .walletId(wallet.getWalletId())
                .userId(wallet.getUser().getUserId())
                .username(wallet.getUser().getUsername())
                .balance(wallet.getBalance())
                .totalEarnings(wallet.getTotalEarnings())
                .pendingBalance(wallet.getPendingBalance())
                .lastUpdated(wallet.getLastUpdated())
                .build();
    }

    private WalletTransactionDTO mapToTransactionDTO(WalletTransaction transaction) {
        return WalletTransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .orderId(transaction.getRelatedOrder() != null ? transaction.getRelatedOrder().getOrderId() : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
