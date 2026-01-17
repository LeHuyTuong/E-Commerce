package com.example.ecom.service.impl;

import com.example.ecom.exceptions.APIException;
import com.example.ecom.exceptions.ResourceNotFoundException;
import com.example.ecom.model.Cart;
import com.example.ecom.model.Category;
import com.example.ecom.model.Product;
import com.example.ecom.model.User;
import com.example.ecom.model.AppRole;
import com.example.ecom.util.AuthUtil;
import com.example.ecom.payload.CartDTO;
import com.example.ecom.payload.ProductDTO;
import com.example.ecom.payload.ProductResponse;
import com.example.ecom.repositories.CategoryRepository;
import com.example.ecom.repositories.ProductRepository;
import com.example.ecom.service.CartService;
import com.example.ecom.service.CategoryService;
import com.example.ecom.service.FileService;
import com.example.ecom.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final FileService fileService;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;

    public ProductServiceImpl(ProductRepository productRepository,
            CategoryService categoryService,
            FileService fileService,
            @Lazy CartService cartService,
            ModelMapper modelMapper,
            AuthUtil authUtil) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.fileService = fileService;
        this.cartService = cartService;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

    @Value("${project.image}")
    private String path;

    @Override
    public Product findByProductId(Long productId) {
        return productRepository.findById(productId).orElseThrow(
                (() -> new ResourceNotFoundException("Product", "Product not found with id: ", productId)));
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Product not found with id: ", productId));

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException("Product", "Product not found with id: ", productId);
        }

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryService.findById(categoryId);

        boolean ifProductNotPresent = true;
        List<Product> products = category.getProducts();
        for (Product value : products) {
            if (value.getProductName().equalsIgnoreCase(productDTO.getProductName())) {
                ifProductNotPresent = false;
                break;
            }
        }

        if (ifProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.png");
            product.setCategory(category);

            BigDecimal discountAmount = product.getPrice()
                    .multiply(product.getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal specialPrice = product.getPrice().subtract(discountAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            product.setSpecialPrice(specialPrice);
            product.setUser(authUtil.loggedInUser()); // Set current user as seller
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        } else {
            throw new APIException("Product already exists with name: " + productDTO.getProductName());
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = (sortOrder.equalsIgnoreCase("asc"))
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByActiveTrue(pageDetails);

        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageSize(productPage.getSize());
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy,
            String sortOrder) {
        Category category = categoryService.findById(categoryId);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy,
            String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%',
                pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (products.isEmpty()) {
            throw new APIException("Products not found with keyword: " + keyword);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;

    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId, Long categoryId) {
        Product productFromDB = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product", "Product not found with id: ", productId));

        // Ownership check - only owner or admin can update
        User currentUser = authUtil.loggedInUser();
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().name().equals(AppRole.ROLE_ADMIN.name()));
        if (!isAdmin && !productFromDB.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new APIException("You do not have permission to update this product");
        }

        Product product = modelMapper.map(productDTO, Product.class);

        productFromDB.setProductName(product.getProductName());
        productFromDB.setDescription(product.getDescription());
        productFromDB.setPrice(product.getPrice());
        productFromDB.setDiscount(product.getDiscount());
        productFromDB.setQuantity(product.getQuantity());

        BigDecimal discountAmount = productFromDB.getPrice()
                .multiply(productFromDB.getDiscount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal specialPrice = productFromDB.getPrice().subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);
        productFromDB.setSpecialPrice(specialPrice);

        if (categoryId != null) {
            Category category = categoryService.findById(categoryId);
            productFromDB.setCategory(category);
        }

        Product savedProduct = productRepository.save(productFromDB);

        List<Cart> carts = cartService.findCartByProductId(productId);

        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                    .collect(Collectors.toList());
            cartDTO.setProducts(products);
            return cartDTO;
        }).collect(Collectors.toList());

        cartDTOS.forEach(cart -> cartService.updateProductInCart(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    @Transactional
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product", "Product not found with id: ", productId));

        User currentUser = authUtil.loggedInUser();

        System.out.println("DEBUG: Deleting product ID: " + productId);
        System.out.println(
                "DEBUG: Product Owner ID: " + (product.getUser() != null ? product.getUser().getUserId() : "NULL"));
        System.out.println("DEBUG: Current User ID: " + currentUser.getUserId());

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().name().equals(AppRole.ROLE_ADMIN.name()));

        System.out.println("DEBUG: Is Admin: " + isAdmin);

        if (!isAdmin) {
            if (product.getUser() == null) {
                // If it has no owner, we don't know if this seller should delete it.
                // For safety, only Admin can delete unowned products.
                throw new APIException("Product has no owner. Please contact Admin.");
            }
            if (!product.getUser().getUserId().equals(currentUser.getUserId())) {
                throw new APIException("You do not have permission to delete this product. Product belongs to user ID: "
                        + product.getUser().getUserId());
            }
        }

        product.setActive(false);
        productRepository.saveAndFlush(product);
        System.out.println("DEBUG: Product marked as inactive successfully.");
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductResponse getProductsBySeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = (sortOrder.equalsIgnoreCase("asc"))
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        User currentUser = authUtil.loggedInUser();
        Page<Product> productPage = productRepository.findByUserAndActiveTrue(currentUser, pageDetails);

        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageSize(productPage.getSize());
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile imageFile) throws IOException {
        Product productFromDb = findByProductId(productId);

        String fileName = fileService.uploadImage(path, imageFile);

        productFromDb.setImage(fileName);
        Product updatedProduct = productRepository.save(productFromDb);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }
}
