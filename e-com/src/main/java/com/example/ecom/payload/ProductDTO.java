package com.example.ecom.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String image;
    private Integer quantity;
    private String description;
    private BigDecimal price = BigDecimal.ZERO;
    private BigDecimal specialPrice = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private CategoryDTO category; // Added for product display
}
