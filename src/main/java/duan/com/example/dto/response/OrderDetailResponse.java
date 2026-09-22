package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private Integer maBienThe;
    private Integer maSP;
    private String tenSP;
    private String hinhAnh;
    private String size;
    private String tenMau;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}