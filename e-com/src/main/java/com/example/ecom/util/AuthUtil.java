package com.example.ecom.util;

import com.example.ecom.model.User;
import com.example.ecom.repositories.UserRepository;
import com.example.ecom.security.service.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Utility for getting current logged-in user information.
 * OPTIMIZED: Uses SecurityContext directly for username/email to avoid DB
 * queries.
 * Only queries DB when full User entity is needed.
 */
@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserRepository userRepository;

    /**
     * Get logged-in user's email directly from UserDetailsImpl (no DB query).
     */
    public String loggedInEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getEmail();
        }
        // Fallback to DB if UserDetailsImpl not available
        return loggedInUser().getEmail();
    }

    /**
     * Get logged-in user's ID directly from UserDetailsImpl (no DB query).
     */
    public Long loggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        // Fallback to DB if UserDetailsImpl not available
        return loggedInUser().getUserId();
    }

    /**
     * Get logged-in username directly from SecurityContext (no DB query).
     */
    public String loggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Get full User entity - this DOES query DB (use sparingly).
     */
    public User loggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + authentication.getName()));
    }
}
