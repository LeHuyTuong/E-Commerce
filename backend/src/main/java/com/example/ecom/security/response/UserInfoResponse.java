package com.example.ecom.security.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Tác dụng: Đây là một lớp Đối tượng Truyền dữ liệu (Data Transfer Object - DTO).
 * Nó đại diện cho cấu trúc dữ liệu được trả về cho client sau khi người dùng đăng nhập thành công.
 *
 * Lớp này chứa các thông tin cần thiết cho phía client để duy trì phiên đăng nhập và
 * hiển thị thông tin người dùng, bao gồm:
 * - ID người dùng
 * - JWT Token để xác thực cho các request tiếp theo
 * - Username
 * - Danh sách các vai trò (roles) của người dùng
 *
 * Các annotation của Lombok được sử dụng để tự động tạo getters, setters, và constructors.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String jwtToken;
    private String username;
    private List<String> roles;

    public UserInfoResponse(Long id, String username, List<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}
