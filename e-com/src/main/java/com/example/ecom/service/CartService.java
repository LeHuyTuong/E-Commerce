package com.example.ecom.service;

import com.example.ecom.model.Cart;
import com.example.ecom.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Long cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

    @Transactional
    String deleteProductFromCart(Long cartId, Long productId);

    List<Cart> findCartByProductId(Long productId);

    void updateProductInCart(Long cartId, Long productId);

    Cart getCartByEmail(String email);
}
