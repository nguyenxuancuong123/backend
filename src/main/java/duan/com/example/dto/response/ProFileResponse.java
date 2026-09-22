package duan.com.example.dto.response;

import duan.com.example.entity.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProFileResponse {

    private Integer id;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private Role vaiTro;
    private LocalDateTime ngayTao;
    private Boolean trangThai;
}
