package com.example.ecom.repositories;

import com.example.ecom.model.User;
import com.example.ecom.model.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserUserId(Long userId);

    boolean existsByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user = :user")
    Optional<Wallet> findByUserWithLock(User user);
}
