package com.example.ecom.repositories;

import com.example.ecom.model.User;
import com.example.ecom.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserUserId(Long userId);

    boolean existsByUser(User user);
}
