package com.example.ecom.security.jwt;

import com.example.ecom.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

/**
 * Tác dụng: Lớp tiện ích này xử lý mọi thứ liên quan đến JSON Web Tokens (JWT).
 * Bao gồm: tạo token, phân tích (parse) token để lấy thông tin, và xác thực
 * (validate) token.
 * Lifecycle: Được quản lý bởi Spring container như một Singleton Bean
 * (@Component).
 */
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Chuỗi bí mật để ký (sign) và xác thực token. Được lấy từ file
    // application.properties.
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    // Thời gian hết hạn của token (tính bằng mili giây). Được lấy từ file
    // application.properties.
    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Tên của cookie sẽ được dùng để lưu trữ JWT.
    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie;

    // public String getJwtFromHeader(HttpServletRequest request) {
    // String bearerToken = request.getHeader("Authorization");
    // logger.debug("Authorization Header: {}", bearerToken);
    // if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
    // return bearerToken.substring(7); // Remove Bearer prefix
    // }
    // return null;
    // }

    /**
     * Tác dụng: Lấy chuỗi JWT từ cookie trong HTTP request.
     * Flow:
     * 1. Duyệt qua các cookie của request.
     * 2. Tìm cookie có tên khớp với `jwtCookie`.
     * 3. Nếu tìm thấy, trả về giá trị (value) của cookie đó.
     * 4. Nếu không, trả về null.
     */
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie); // Dùng tiện ích của Spring để lấy cookie
        if (cookie != null) {
            System.out.println("Cookie: " + cookie.getValue());
            return cookie.getValue();
        } else {
            return null;
        }
    }

    /**
     * Tác dụng: Tạo một đối tượng ResponseCookie chứa JWT.
     * Flow:
     * 1. Gọi `generateTokenFromUsername` để tạo chuỗi JWT.
     * 2. Xây dựng một `ResponseCookie` với các thuộc tính:
     * - Tên: `jwtCookie`
     * - Giá trị: chuỗi JWT vừa tạo.
     * - Path: "/api" (cookie chỉ được gửi cho các request đến /api).
     * - Max-Age: Thời gian sống của cookie.
     * - httpOnly(false): Cho phép JavaScript phía client có thể truy cập cookie
     * này.
     * (Lưu ý: Đặt là `true` sẽ an toàn hơn để chống lại tấn công XSS).
     */
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/") // Cookie sent for ALL paths
                .maxAge(24 * 60 * 60 * 10) // 10 days
                .httpOnly(true) // SECURITY: Prevent XSS
                .secure(false) // DEV ONLY: Set to true in production with HTTPS
                .sameSite("Lax") // SECURITY: Prevent CSRF
                .build();

        return cookie;
    }

    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
                .path("/") // Must match the cookie path used in generateJwtCookie
                .build();
        return cookie;
    }

    /**
     * Tác dụng: Tạo ra một chuỗi JWT từ username.
     * Flow:
     * 1. Sử dụng thư viện JJWT để xây dựng token.
     * 2. Thiết lập `subject` của token là username.
     * 3. Thiết lập thời gian phát hành (`issuedAt`) là thời điểm hiện tại.
     * 4. Thiết lập thời gian hết hạn (`expiration`) bằng thời gian hiện tại +
     * `jwtExpirationMs`.
     * 5. Ký token bằng thuật toán HMAC-SHA và khóa bí mật (`key()`).
     * 6. Hoàn tất và trả về chuỗi token.
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /**
     * Tác dụng: Lấy username từ một chuỗi JWT.
     * Flow:
     * 1. Phân tích (parse) chuỗi token.
     * 2. Xác thực chữ ký của token bằng khóa bí mật.
     * 3. Lấy ra phần `payload` (claims).
     * 4. Trả về giá trị của `subject`, chính là username.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    /**
     * Tác dụng: Chuyển đổi chuỗi `jwtSecret` (định dạng Base64) thành một đối tượng
     * `Key` để sử dụng cho việc ký và xác thực.
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Tác dụng: Kiểm tra xem một chuỗi JWT có hợp lệ hay không.
     * Flow:
     * 1. Cố gắng phân tích (parse) và xác thực token bằng khóa bí mật.
     * 2. Nếu thành công (không có exception nào được ném ra), token là hợp lệ ->
     * trả về `true`.
     * 3. Nếu có bất kỳ lỗi nào xảy ra trong quá trình xác thực (token sai định
     * dạng, hết hạn, chữ ký không khớp...),
     * một exception tương ứng sẽ được ném ra.
     * 4. Bắt các exception này, ghi log lỗi và trả về `false`.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
