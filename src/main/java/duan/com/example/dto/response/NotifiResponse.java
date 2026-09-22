package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifiResponse {
    private Integer maThongBao;
    private Integer maDonHang;
    private String trangThai;
    private boolean daDoc;
    private LocalDateTime thoiGian;
}
