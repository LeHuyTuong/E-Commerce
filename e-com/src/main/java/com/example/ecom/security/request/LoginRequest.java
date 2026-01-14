package com.example.ecom.security.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Tác dụng: Đây là một lớp Đối tượng Truyền dữ liệu (Data Transfer Object - DTO).
 * Nó đại diện cho cấu trúc dữ liệu mà client (ví dụ: frontend) phải gửi lên
 * khi thực hiện yêu cầu đăng nhập tại endpoint `/api/auth/signin`.
 *
 * Sử dụng `@NotBlank` để đảm bảo cả `username` và `password` không được để trống hoặc null.
 * Spring Validation sẽ tự động kiểm tra các ràng buộc này khi request được xử lý bởi Controller.
 */
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
