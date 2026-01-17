package com.example.ecom.service;

import com.example.ecom.model.Product;
import com.example.ecom.payload.ProductDTO;
import com.example.ecom.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

        Product findByProductId(Long productId);

        ProductDTO getProductById(Long productId);

        ProductDTO addProduct(Long categoryId, ProductDTO productDTO);

        ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

        ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy,
                        String sortOrder);

        ProductResponse searchByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy,
                        String sortOrder);

        ProductDTO updateProduct(ProductDTO productDTO, Long productId, Long categoryId);

        ProductDTO deleteProduct(Long productId);

        ProductDTO updateProductImage(Long productId, MultipartFile imageFile) throws IOException;

        ProductResponse getProductsBySeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
