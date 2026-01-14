package com.example.ecom.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JWT Authentication Filter with DEBUG logging to file
 */
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    // Debug log file path
    private static final String DEBUG_LOG_FILE = "auth-debug.log";

    private void debugLog(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(DEBUG_LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            out.println("[" + timestamp + "] " + message);
        } catch (IOException e) {
            // Ignore
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        // Log to file
        debugLog("========================================");
        debugLog("REQUEST: " + method + " " + uri);
        debugLog("Authorization Header: "
                + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "NULL"));

        // Log cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().contains("jwt") || cookie.getName().contains("ecom")) {
                    debugLog("Cookie: " + cookie.getName() + " = "
                            + cookie.getValue().substring(0, Math.min(30, cookie.getValue().length())) + "...");
                }
            }
        } else {
            debugLog("Cookies: NULL");
        }

        try {
            String jwt = parseJwt(request);
            debugLog("JWT Extracted: "
                    + (jwt != null ? "YES (" + jwt.substring(0, Math.min(30, jwt.length())) + "...)" : "NO"));

            if (jwt == null) {
                debugLog("RESULT: NO JWT -> UNAUTHENTICATED");
            } else if (!jwtUtils.validateJwtToken(jwt)) {
                debugLog("RESULT: JWT INVALID");
            } else {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                debugLog("RESULT: JWT VALID for user: " + username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                debugLog("SecurityContext SET for: " + username);
            }
        } catch (Exception e) {
            debugLog("ERROR: " + e.getMessage());
        }

        debugLog("========================================");
        try {
            filterChain.doFilter(request, response);
            // Log response status after filter chain completes
            debugLog("AFTER doFilter - Response Status: " + response.getStatus());
        } catch (Exception e) {
            debugLog("EXCEPTION in doFilter: " + e.getClass().getName() + " - " + e.getMessage());
            throw e;
        }
    }

    private String parseJwt(HttpServletRequest request) {
        // 1. Try Authorization header first
        String headerAuth = request.getHeader("Authorization");

        // DETAILED DEBUG
        debugLog("parseJwt - headerAuth raw: [" + headerAuth + "]");
        debugLog("parseJwt - hasText: " + StringUtils.hasText(headerAuth));
        if (headerAuth != null) {
            debugLog("parseJwt - startsWith 'Bearer ': " + headerAuth.startsWith("Bearer "));
            debugLog("parseJwt - length: " + headerAuth.length());
        }

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            debugLog("JWT Source: Authorization Header");
            return headerAuth.substring(7);
        }

        // 2. Fallback to cookie
        String cookieJwt = jwtUtils.getJwtFromCookies(request);
        if (cookieJwt != null) {
            debugLog("JWT Source: Cookie");
            return cookieJwt;
        }

        debugLog("JWT Source: NONE FOUND");
        return null;
    }
}
