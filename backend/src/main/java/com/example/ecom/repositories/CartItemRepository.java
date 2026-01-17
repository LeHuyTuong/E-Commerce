package com.example.ecom.repositories;

import com.example.ecom.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("Select ci From CartItem ci where ci.product.productId = ?1 And ci.cart.cartId = ?2")
    CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId);

    @Modifying
    @Query(value = "DELETE FROM cart_items WHERE product_id = ?1 AND cart_id = ?2", nativeQuery = true)
    void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);
}
