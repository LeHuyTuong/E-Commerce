package com.example.ecom.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tác dụng: Đây là một "điểm vào" (Entry Point) xử lý lỗi xác thực.
 * Nó được kích hoạt bất cứ khi nào một người dùng chưa được xác thực (unauthenticated)
 * cố gắng truy cập vào một tài nguyên yêu cầu xác thực (authenticated).
 * Lifecycle: Được Spring quản lý như một Bean (@Component) và được cấu hình trong `WebSecurityConfig`.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * Tác dụng: Ghi đè phương thức `commence` để tùy chỉnh phản hồi lỗi.
     * Thay vì chuyển hướng đến trang đăng nhập (hành vi mặc định cho ứng dụng web truyền thống),
     * phương thức này sẽ trả về một phản hồi lỗi 401 Unauthorized dưới dạng JSON, phù hợp cho các API RESTful.
     *
     * Flow hoạt động:
     * 1. Spring Security's ExceptionTranslationFilter bắt một `AuthenticationException` (hoặc AccessDeniedException).
     * 2. Vì người dùng chưa được xác thực, nó sẽ ủy quyền cho `AuthenticationEntryPoint` đã được cấu hình.
     * 3. Phương thức `commence` này được gọi.
     * 4. Nó set HTTP status của response thành 401 (SC_UNAUTHORIZED).
     * 5. Set Content-Type là `application/json`.
     * 6. Tạo một đối tượng Map chứa thông tin chi tiết về lỗi (status, error, message, path).
     * 7. Dùng `ObjectMapper` để chuyển đổi Map thành chuỗi JSON và ghi trực tiếp vào `OutputStream` của response.
     *
     * @param request       Request đã gây ra lỗi xác thực.
     * @param response      Response để chúng ta có thể tùy chỉnh.
     * @param authException Exception chứa thông tin về lỗi xác thực.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());

        // Set kiểu nội dung trả về là JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Set mã trạng thái HTTP là 401 - Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Tạo một body cho response JSON để cung cấp thông tin lỗi rõ ràng hơn
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        // Sử dụng Jackson ObjectMapper để ghi Map dưới dạng JSON vào luồng output của response
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }

}
