package com.example.ecom.security.service;

import com.example.ecom.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tác dụng: Đây là lớp triển khai (implementation) của interface `UserDetails` từ Spring Security.
 * Nó đóng vai trò là "adapter", chuyển đổi đối tượng `User` (model của ứng dụng) thành một đối tượng
 * mà Spring Security có thể hiểu và làm việc, chứa tất cả thông tin cần thiết cho việc xác thực và phân quyền.
 *
 * Lifecycle: Một instance của `UserDetailsImpl` được tạo ra bởi `UserDetailsServiceImpl` mỗi khi cần
 * tải thông tin người dùng. Nó tồn tại trong suốt quá trình xử lý một request đã được xác thực,
 * được lưu trong `SecurityContext`.
 */
@NoArgsConstructor
@Data
public class UserDetailsImpl implements UserDetails {

    // serialVersionUID được sử dụng trong quá trình tuần tự hóa (serialization).
    // Nó đảm bảo rằng một đối tượng đã được tuần tự hóa có thể được giải tuần tự hóa một cách chính xác,
    // ngay cả khi có sự thay đổi trong mã nguồn của lớp.
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    // Annotation @JsonIgnore là cực kỳ quan trọng.
    // Nó ngăn không cho trường `password` bị chuyển thành JSON khi đối tượng này được trả về qua API.
    // Điều này giúp tránh làm lộ mật khẩu đã được hash của người dùng.
    @JsonIgnore
    private String password;

    // Collection này chứa các quyền (authorities) của người dùng (ví dụ: ROLE_USER, ROLE_ADMIN).
    // Spring Security sử dụng collection này để thực hiện việc phân quyền (authorization).
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Tác dụng: Phương thức factory tĩnh, dùng để tạo một instance `UserDetailsImpl` từ một `User` entity.
     * Đây là một best practice để tách biệt logic tạo đối tượng ra khỏi constructor.
     *
     * Flow:
     * 1. Lấy danh sách các đối tượng `Role` từ `User` entity.
     * 2. Dùng Stream API để chuyển đổi mỗi `Role` thành một đối tượng `SimpleGrantedAuthority`.
     *    `SimpleGrantedAuthority` là một triển khai cơ bản của `GrantedAuthority` mà Spring Security cung cấp.
     *    Tên của quyền cần phải ở định dạng mà Spring Security mong đợi (ví dụ: "ROLE_USER").
     * 3. Thu thập các `GrantedAuthority` vào một List.
     * 4. Gọi constructor để tạo và trả về một đối tượng `UserDetailsImpl` mới.
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Các phương thức dưới đây cho phép kiểm soát trạng thái của tài khoản một cách chi tiết hơn.
    // Trong ứng dụng này, chúng ta mặc định trả về `true`, nghĩa là tài khoản luôn hợp lệ.
    // Trong một ứng dụng thực tế, bạn có thể triển khai logic phức tạp hơn,
    // ví dụ: kiểm tra một trường `isExpired` hoặc `isLocked` trong `User` entity.

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không bao giờ hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Tài khoản không bao giờ bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Thông tin xác thực (mật khẩu) không bao giờ hết hạn
    }

    @Override
    public boolean isEnabled() {
        return true; // Tài khoản luôn được kích hoạt
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
