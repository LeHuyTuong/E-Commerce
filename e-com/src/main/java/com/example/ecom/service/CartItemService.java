package com.example.ecom.service;

import com.example.ecom.model.CartItem;

public interface CartItemService {
    CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId);
    void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);

}
