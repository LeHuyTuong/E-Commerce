package com.example.ecom.security.service;

import com.example.ecom.model.User;
import com.example.ecom.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tác dụng: Đây là một Service cốt lõi của Spring Security, có nhiệm vụ cung cấp dữ liệu người dùng.
 * Nó đóng vai trò là cầu nối giữa model User của ứng dụng và UserDetails của Spring Security.
 * Lifecycle: Được khởi tạo như một Spring Bean (@Service) và được DaoAuthenticationProvider sử dụng
 * mỗi khi cần xác thực thông tin người dùng.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Tác dụng: Tìm và tải thông tin chi tiết của một người dùng từ database dựa vào username.
     * Flow hoạt động:
     * 1. Khi người dùng thực hiện đăng nhập, `AuthenticationManager` sẽ gọi `DaoAuthenticationProvider`.
     * 2. `DaoAuthenticationProvider` sẽ gọi phương thức `loadUserByUsername` này để lấy thông tin người dùng (bao gồm cả mật khẩu đã mã hóa và quyền).
     * 3. Framework sẽ tự động so sánh mật khẩu người dùng nhập vào (sau khi đã mã hóa) với mật khẩu trong đối tượng `UserDetails` được trả về từ đây.
     *
     * @param username Tên đăng nhập mà người dùng cung cấp.
     * @return một đối tượng UserDetails chứa thông tin đầy đủ của người dùng (id, username, password, roles).
     * @throws UsernameNotFoundException nếu không tìm thấy người dùng trong database.
     */
    @Override
    @Transactional // Đảm bảo các thao tác với DB (ví dụ: lazy loading roles) được thực hiện trong cùng một transaction.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tìm kiếm User entity trong database thông qua UserRepository.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        // 2. Chuyển đổi từ User entity của ứng dụng sang đối tượng UserDetails mà Spring Security có thể hiểu và sử dụng.
        return UserDetailsImpl.build(user);
    }
}
