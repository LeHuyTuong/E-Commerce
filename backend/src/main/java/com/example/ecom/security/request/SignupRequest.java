package com.example.ecom.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * Tác dụng: Đây là một lớp Đối tượng Truyền dữ liệu (Data Transfer Object - DTO).
 * Nó định nghĩa cấu trúc dữ liệu mà client phải gửi lên khi thực hiện yêu cầu đăng ký
 * tài khoản mới tại endpoint `/api/auth/signup`.
 *
 * Các annotation từ `jakarta.validation.constraints` (như @NotBlank, @Size, @Email)
 * được sử dụng để xác thực dữ liệu đầu vào, đảm bảo dữ liệu tuân thủ các quy tắc nghiệp vụ
 * trước khi được xử lý.
 *
 * Các annotation từ Lombok (@Getter, @Setter, @Data...) được dùng để tự động sinh code
 * cho các phương thức getter, setter, constructor, toString, v.v., giúp mã nguồn gọn gàng hơn.
 */
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    // Set<String> chứa các vai trò (role) mà người dùng muốn đăng ký, ví dụ: ["admin", "user"].
    // Nếu không được cung cấp, logic trong AuthController sẽ gán vai trò mặc định.
    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
