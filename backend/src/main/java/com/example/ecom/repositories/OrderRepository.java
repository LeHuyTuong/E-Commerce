package com.example.ecom.repositories;

import com.example.ecom.model.Order;
import com.example.ecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    Double getTotalRevenue();

    List<Order> findByEmailOrderByOrderDateDesc(String email);

    List<Order> findBySellerOrderByOrderDateDesc(User seller);

    // For Admin: get all orders
    List<Order> findAllByOrderByOrderDateDesc();
}
