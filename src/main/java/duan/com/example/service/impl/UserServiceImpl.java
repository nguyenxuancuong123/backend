package duan.com.example.service.impl;

import duan.com.example.dto.response.ProFileResponse;
import duan.com.example.entity.Users;
import duan.com.example.entity.role.Role;
import duan.com.example.repository.UserRepository;
import duan.com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // tìm kiếm thông tin người dùng từ csdl
    @Override
    public UserDetailsService userDetailService(){
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username)  {
                return userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            }
        };
    }

    @Override
    public ProFileResponse getHoSo(String email) {
        Users users = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + email));
        return mapToHoSoResponse(users);
    }

    @Override
    public Page<ProFileResponse> getUsersPaginated(int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("id").descending());
        return userRepository.findAll(pageable).map(this::mapToHoSoResponse);
    }

    @Override
    public ProFileResponse getUserById(Integer id) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + id));
        return mapToHoSoResponse(users);
    }

    @Override
    public ProFileResponse updateUserRole(Integer id, Role vaiTro) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + id));
        users.setVaiTro(vaiTro);
        userRepository.save(users);
        return mapToHoSoResponse(users);
    }

    @Override
    public ProFileResponse updateUserStatus(Integer id, Boolean trangThai) {
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + id));
        users.setTrangThai(trangThai);
        userRepository.save(users);
        return mapToHoSoResponse(users);
    }

    private ProFileResponse mapToHoSoResponse(Users users) {
        return ProFileResponse.builder()
                .id(users.getId())
                .hoTen(users.getHoten())
                .email(users.getEmail())
                .soDienThoai(users.getSoDienThoai())
                .diaChi(users.getDiaChi())
                .vaiTro(users.getVaiTro())
                .ngayTao(users.getNgayTao())
                .trangThai(users.getTrangThai())
                .build();
    }
}
