package com.example.ecom.repositories;

import com.example.ecom.model.Wallet;
import com.example.ecom.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);

    Page<WalletTransaction> findByWalletOrderByCreatedAtDesc(Wallet wallet, Pageable pageable);

    List<WalletTransaction> findByWalletAndTypeOrderByCreatedAtDesc(Wallet wallet,
            WalletTransaction.TransactionType type);

    @Query("SELECT SUM(t.amount) FROM WalletTransaction t WHERE t.wallet = ?1 AND t.type = ?2")
    BigDecimal sumByWalletAndType(Wallet wallet, WalletTransaction.TransactionType type);
}
