package com.example.ecom.repositories;

import com.example.ecom.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // vi jpa khong ho tro query khi cac object long nhau nen phai su dung query de tim
    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1")
    Cart findCartByEmail(String email);

    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1 AND c.cartId = ?2")
    Cart findCartByEmailAndCartId(String emailId, Long cartId);

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.productId = ?1")
        // join fetch de lay du lieu luon ko bi lazy
        //lazy la co che tai du lieu khi can thiet
    List<Cart> findCartByProductId(Long productId);

}
