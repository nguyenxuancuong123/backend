package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Integer maGioHang;
    private Integer maBienThe;
    private Integer maSP;
    private String tenSP;
    private String hinhAnh;
    private String size;
    private String tenMau;
    private BigDecimal giaBan;      // giá áp dụng (đã tính khuyến mãi nếu có)
    private Integer soLuong;
    private Integer soLuongTon;
    private BigDecimal thanhTien;
    private LocalDateTime ngayThem;
}