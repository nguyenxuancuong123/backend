package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatus {
    private Integer maThongBao;
    private Integer maDonHang;
    private String trangThai;
}
