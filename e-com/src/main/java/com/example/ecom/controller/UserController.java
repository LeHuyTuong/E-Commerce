package com.example.ecom.controller;

import com.example.ecom.model.User;
import com.example.ecom.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Map to simple response to avoid exposing password
        List<UserDTO> userDTOs = users.stream()
                .map(u -> new UserDTO(
                        u.getUserId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRoles().stream()
                                .map(r -> r.getRoleName().name())
                                .collect(Collectors.toSet())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    // Simple DTO to avoid exposing password
    record UserDTO(Long userId, String username, String email, java.util.Set<String> roles) {
    }
}
