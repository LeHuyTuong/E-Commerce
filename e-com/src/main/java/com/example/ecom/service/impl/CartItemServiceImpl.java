package com.example.ecom.service.impl;

import com.example.ecom.model.CartItem;
import com.example.ecom.repositories.CartItemRepository;
import com.example.ecom.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId) {
        return cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
    }

    @Override
    @Transactional
    public void deleteCartItemByProductIdAndCartId(Long productId, Long cartId) {
        // Use JPA repository method instead of JdbcTemplate to maintain transaction
        // context
        cartItemRepository.deleteCartItemByProductIdAndCartId(productId, cartId);
    }

}
