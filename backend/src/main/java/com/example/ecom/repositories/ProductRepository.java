package com.example.ecom.repositories;

import com.example.ecom.model.Category;
import com.example.ecom.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetails);

    Page<Product> findByProductNameLikeIgnoreCase(String productName, Pageable pageDetails);

    // For public API - only active products
    Page<Product> findByActiveTrue(Pageable pageable);

    // Active products by category
    Page<Product> findByCategoryAndActiveTrue(Category category, Pageable pageable);

    // Active products by keyword
    Page<Product> findByProductNameLikeIgnoreCaseAndActiveTrue(String productName, Pageable pageable);

    // Active products by seller
    Page<Product> findByUserAndActiveTrue(com.example.ecom.model.User user, Pageable pageable);
}
