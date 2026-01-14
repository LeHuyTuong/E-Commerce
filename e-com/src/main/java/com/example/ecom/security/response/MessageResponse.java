package com.example.ecom.security.response;

/**
 * Tác dụng: Đây là một lớp Đối tượng Truyền dữ liệu (Data Transfer Object - DTO) đơn giản.
 * Nó được sử dụng để đóng gói một thông báo (message) dưới dạng JSON để trả về cho client.
 *
 * Ví dụ: Sau khi đăng ký thành công, server có thể trả về:
 * {
 *   "message": "User registered successfully!"
 * }
 */
public class MessageResponse {
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
