package com.example.ecom.service.impl;

import com.example.ecom.exceptions.APIException;
import com.example.ecom.exceptions.ResourceNotFoundException;
import com.example.ecom.model.Cart;
import com.example.ecom.model.CartItem;
import com.example.ecom.model.Product;
import com.example.ecom.payload.CartDTO;
import com.example.ecom.payload.ProductDTO;
import com.example.ecom.repositories.CartItemRepository;
import com.example.ecom.repositories.CartRepository;
import com.example.ecom.service.CartItemService;
import com.example.ecom.service.CartService;
import com.example.ecom.service.ProductService;
import com.example.ecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int SCALE = 2;

    private final CartRepository cartRepository;
    private final AuthUtil authUtil;
    private final ProductService productService;
    private final CartItemService cartItemService;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;

    public CartServiceImpl(CartRepository cartRepository, AuthUtil authUtil,
            @Lazy ProductService productService,
            CartItemService cartItemService,
            CartItemRepository cartItemRepository,
            ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.authUtil = authUtil;
        this.productService = productService;
        this.cartItemService = cartItemService;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();
        Product product = productService.findByProductId(productId);
        CartItem cartItem = cartItemService.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity());
        }

        BigDecimal priceIncrease = product.getSpecialPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(SCALE, ROUNDING_MODE);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + quantity;
            if (product.getQuantity() < newQuantity) {
                throw new APIException("Not enough stock. Available: " + product.getQuantity());
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            cart.setTotalPrice(cart.getTotalPrice().add(priceIncrease).setScale(SCALE, ROUNDING_MODE));
            cartRepository.save(cart);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setProduct(product);
            newCartItem.setCart(cart);
            newCartItem.setQuantity(quantity);
            newCartItem.setDiscount(product.getDiscount());
            newCartItem.setProductPrice(product.getSpecialPrice());

            cart.getCartItems().add(newCartItem);
            cart.setTotalPrice(cart.getTotalPrice().add(priceIncrease).setScale(SCALE, ROUNDING_MODE));

            cartRepository.save(cart);
        }

        return mapCartToDTO(cart);
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No cart exist");
        }

        return carts.stream().map(this::mapCartToDTO).collect(Collectors.toList());
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        return mapCartToDTO(cart);
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productService.findByProductId(productId);

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity());
        }

        CartItem cartItem = getCartItem(productId, cartId);
        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " does not exist in cart");
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(newQuantity);
            cartItem.setDiscount(product.getDiscount());

            BigDecimal priceChange = cartItem.getProductPrice()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(SCALE, ROUNDING_MODE);
            cart.setTotalPrice(cart.getTotalPrice().add(priceChange).setScale(SCALE, ROUNDING_MODE));
            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);
        if (updatedItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        return mapCartToDTO(cart);
    }

    private CartItem getCartItem(Long productId, Long cartId) {
        return cartItemService.findCartItemByProductIdAndCartId(productId, cartId);
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemService.findCartItemByProductIdAndCartId(productId, cartId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "ProductId", productId);
        }

        cart.getCartItems().remove(cartItem);

        BigDecimal priceDecrease = cartItem.getProductPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                .setScale(SCALE, ROUNDING_MODE);
        cart.setTotalPrice(cart.getTotalPrice().subtract(priceDecrease).setScale(SCALE, ROUNDING_MODE));

        cartRepository.save(cart);

        return "Product " + cartItem.getProduct().getProductName() + " has been deleted";
    }

    @Override
    public List<Cart> findCartByProductId(Long productId) {
        return cartRepository.findCartByProductId(productId);
    }

    @Override
    @Transactional
    public void updateProductInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productService.findByProductId(productId);
        CartItem cartItem = cartItemService.findCartItemByProductIdAndCartId(productId, cartId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " does not exist in cart");
        }

        BigDecimal oldItemTotal = cartItem.getProductPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal cartPriceWithoutItem = cart.getTotalPrice().subtract(oldItemTotal);

        cartItem.setProductPrice(product.getSpecialPrice());

        BigDecimal newItemTotal = cartItem.getProductPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        cart.setTotalPrice(cartPriceWithoutItem.add(newItemTotal).setScale(SCALE, ROUNDING_MODE));

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public Cart getCartByEmail(String email) {
        return cartRepository.findCartByEmail(email);
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null) {
            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }

    private CartDTO mapCartToDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        List<ProductDTO> products = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                })
                .collect(Collectors.toList());

        cartDTO.setProducts(products);
        return cartDTO;
    }
}
