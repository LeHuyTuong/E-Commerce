package com.example.ecom.controller;

import com.example.ecom.model.Cart;
import com.example.ecom.payload.CartDTO;
import com.example.ecom.repositories.CartRepository;
import com.example.ecom.service.CartService;
import com.example.ecom.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AuthUtil authUtil;
    private final CartRepository cartRepository;

    @PostMapping("/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
            @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOS = cartService.getAllCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOS, HttpStatus.OK);
    }

    @GetMapping("/users/cart")
    public ResponseEntity<CartDTO> getCartById() {
        String emailId = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        if (cart == null) {
            CartDTO emptyCart = new CartDTO();
            emptyCart.setProducts(List.of());
            emptyCart.setTotalPrice(java.math.BigDecimal.ZERO);
            return new ResponseEntity<>(emptyCart, HttpStatus.OK);
        }
        Long cartId = cart.getCartId();
        CartDTO cartDTO = cartService.getCart(emailId, cartId);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId,
            @PathVariable String operation) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,
            @PathVariable Long productId) {
        System.out.println("DEBUG: deleteProductFromCart called - cartId=" + cartId + ", productId=" + productId);
        System.out.println("DEBUG: Current user email: " + authUtil.loggedInEmail());
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }
}
