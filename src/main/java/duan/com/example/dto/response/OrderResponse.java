package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer maDonHang;
    private String trangThai;
    private BigDecimal tongTien;
    private LocalDateTime ngayDat;
    private String tenNguoiNhan;
    private String soDienThoaiNhan;
    private String diaChiGiaoHang;
    private String phuongThucThanhToan;
    private List<OrderDetailResponse> chiTiet;
}