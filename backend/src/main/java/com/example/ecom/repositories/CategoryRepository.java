package com.example.ecom.repositories;

import com.example.ecom.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);

    // For public API - only active categories
    Page<Category> findByActiveTrue(Pageable pageable);
}
