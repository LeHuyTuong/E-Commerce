package com.example.ecom.controller;

import com.example.ecom.model.AppRole;
import com.example.ecom.model.Role;
import com.example.ecom.model.User;
import com.example.ecom.repositories.RoleRepository;
import com.example.ecom.repositories.UserRepository;
import com.example.ecom.security.request.LoginRequest;
import com.example.ecom.security.request.SignupRequest;
import com.example.ecom.security.response.MessageResponse;
import com.example.ecom.security.response.UserInfoResponse;
import com.example.ecom.security.jwt.JwtUtils;
import com.example.ecom.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username or password"));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate raw JWT token for response body (localStorage)
        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        // Also generate cookie as fallback
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Return raw JWT token in body AND set cookie as fallback
        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                jwtToken, // Raw JWT token, NOT cookie string
                userDetails.getUsername(),
                roles);

        log.info("User {} logged in successfully. JWT returned in body and cookie.", userDetails.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString()) // Cookie fallback
                .body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // FIX: Return consistent MessageResponse for all error cases
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Username is already taken"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Email is already in use"));
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        try {
            if (strRoles == null) {
                Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(() -> new IllegalStateException("Role ROLE_USER not found in database"));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                    .orElseThrow(
                                            () -> new IllegalStateException("Role ROLE_ADMIN not found in database"));
                            roles.add(adminRole);
                            break;
                        case "seller":
                            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                    .orElseThrow(
                                            () -> new IllegalStateException("Role ROLE_SELLER not found in database"));
                            roles.add(sellerRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                    .orElseThrow(
                                            () -> new IllegalStateException("Role ROLE_USER not found in database"));
                            roles.add(userRole);
                    }
                });
            }
        } catch (IllegalStateException e) {
            // FIX: Log and return proper error instead of 500
            log.error("Role configuration error: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Server configuration error. Please contact administrator."));
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {
        if (authentication != null) {
            return authentication.getName();
        } else {
            return "";
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> signoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You have been signed out"));
    }
}
