package duan.com.example.service;

import duan.com.example.dto.response.ProFileResponse;
import duan.com.example.entity.role.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {

    UserDetailsService userDetailService();

    // Lấy thông tin hồ sơ của bất kỳ user nào dựa theo email (username)
    ProFileResponse getHoSo(String email);

    // Lấy danh sách tất cả người dùng
    List<ProFileResponse> getAllUsers();

    // Lấy thông tin người dùng theo ID
    ProFileResponse getUserById(Integer id);

    // Cập nhật vai trò (Role) của người dùng
    ProFileResponse updateUserRole(Integer id, Role vaiTro);

    // Cập nhật trạng thái (Active/Inactive) của người dùng
    ProFileResponse updateUserStatus(Integer id, Boolean trangThai);
}
