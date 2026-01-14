package com.example.ecom.security;

import com.example.ecom.model.AppRole;
import com.example.ecom.model.Role;
import com.example.ecom.model.User;
import com.example.ecom.repositories.RoleRepository;
import com.example.ecom.repositories.UserRepository;
import com.example.ecom.security.jwt.AuthEntryPointJwt;
import com.example.ecom.security.jwt.AuthTokenFilter;
import com.example.ecom.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
            var corsConfig = new org.springframework.web.cors.CorsConfiguration();
            corsConfig.setAllowedOrigins(java.util.List.of("http://localhost:5173", "http://localhost:5174"));
            corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            corsConfig.setAllowedHeaders(java.util.List.of(
                    "Authorization",
                    "Content-Type",
                    "Accept",
                    "Origin",
                    "X-Requested-With",
                    "Cache-Control",
                    "Pragma",
                    "Expires"));
            corsConfig.setExposedHeaders(java.util.List.of("Authorization", "Set-Cookie"));
            corsConfig.setAllowCredentials(true);
            return corsConfig;
        }))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/v3/api-docs").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/carts/**").authenticated()
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());

        http.addFilterBefore(authTokenFilter,
                UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(
                HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers(
                "/v2/api-docs",
                "/swagger-resources/**",
                "configuration/security",
                "/swagger-ui.html",
                "webjars/**"));
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository,
            com.example.ecom.repositories.CategoryRepository categoryRepository,
            com.example.ecom.repositories.ProductRepository productRepository) {
        return args -> {

            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> {
                        Role newUserRole = new Role(AppRole.ROLE_USER);
                        return roleRepository.save(newUserRole);
                    });
            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                    .orElseGet(() -> {
                        Role newSellerRole = new Role(AppRole.ROLE_SELLER);
                        return roleRepository.save(newSellerRole);
                    });

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role newAdminRole = new Role(AppRole.ROLE_ADMIN);
                        return roleRepository.save(newAdminRole);
                    });

            Set<Role> userRoles = Set.of(userRole);
            Set<Role> sellerRoles = Set.of(sellerRole);
            Set<Role> adminRoles = Set.of(userRole, sellerRole, adminRole);

            User user1 = null;
            if (!userRepository.existsByUsername("user1")) {
                user1 = new User("user1", "user1@gmail.com", passwordEncoder().encode("123456"));
                userRepository.save(user1);
            } else {
                user1 = userRepository.findByUsername("user1").orElse(null);
            }

            User seller1 = null;
            if (!userRepository.existsByUsername("seller1")) {
                seller1 = new User("seller1", "seller1@gmail.com", passwordEncoder().encode("123456"));
                userRepository.save(seller1);
            } else {
                seller1 = userRepository.findByUsername("seller1").orElse(null);
            }

            User admin1 = null;
            if (!userRepository.existsByUsername("admin1")) {
                admin1 = new User("admin1", "admin1@gmail.com", passwordEncoder().encode("123456"));
                userRepository.save(admin1);
            } else {
                admin1 = userRepository.findByUsername("admin1").orElse(null);
            }

            if (user1 != null) {
                user1.setRoles(userRoles);
                userRepository.save(user1);
            }

            if (seller1 != null) {
                seller1.setRoles(sellerRoles);
                userRepository.save(seller1);
            }

            if (admin1 != null) {
                admin1.setRoles(adminRoles);
                userRepository.save(admin1);
            }

            com.example.ecom.model.Category electronics = categoryRepository.findByCategoryName("Electronics");
            if (electronics == null) {
                electronics = new com.example.ecom.model.Category();
                electronics.setCategoryName("Electronics");
                electronics = categoryRepository.save(electronics);
            }

            com.example.ecom.model.Category fashion = categoryRepository.findByCategoryName("Fashion");
            if (fashion == null) {
                fashion = new com.example.ecom.model.Category();
                fashion.setCategoryName("Fashion");
                fashion = categoryRepository.save(fashion);
            }

            com.example.ecom.model.Category home = categoryRepository.findByCategoryName("Home & Kitchen");
            if (home == null) {
                home = new com.example.ecom.model.Category();
                home.setCategoryName("Home & Kitchen");
                home = categoryRepository.save(home);
            }

            if (seller1 != null) {
                if (productRepository.count() == 0) {
                    com.example.ecom.model.Product p1 = new com.example.ecom.model.Product();
                    p1.setProductName("Smartphone X");
                    p1.setDescription("Latest model smartphone with AI features");
                    p1.setQuantity(100);
                    p1.setPrice(new java.math.BigDecimal("999.00"));
                    p1.setDiscount(new java.math.BigDecimal("10.00"));
                    p1.setSpecialPrice(new java.math.BigDecimal("899.10"));
                    p1.setCategory(electronics);
                    p1.setUser(seller1);
                    p1.setImage("default-product.png");
                    productRepository.save(p1);

                    com.example.ecom.model.Product p2 = new com.example.ecom.model.Product();
                    p2.setProductName("Designer T-Shirt");
                    p2.setDescription("Premium cotton t-shirt");
                    p2.setQuantity(50);
                    p2.setPrice(new java.math.BigDecimal("49.00"));
                    p2.setDiscount(java.math.BigDecimal.ZERO);
                    p2.setSpecialPrice(new java.math.BigDecimal("49.00"));
                    p2.setCategory(fashion);
                    p2.setUser(seller1);
                    p2.setImage("default-product.png");
                    productRepository.save(p2);

                    com.example.ecom.model.Product p3 = new com.example.ecom.model.Product();
                    p3.setProductName("Coffee Maker");
                    p3.setDescription("Automatic coffee maker with timer");
                    p3.setQuantity(30);
                    p3.setPrice(new java.math.BigDecimal("120.00"));
                    p3.setDiscount(new java.math.BigDecimal("15.00"));
                    p3.setSpecialPrice(new java.math.BigDecimal("102.00"));
                    p3.setCategory(home);
                    p3.setUser(seller1);
                    p3.setImage("default-product.png");
                    productRepository.save(p3);
                }
            }
        };
    }
}
