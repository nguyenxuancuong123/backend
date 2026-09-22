package duan.com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private String tenNguoiNhan;
    private String soDienThoaiNhan;
    private String diaChiGiaoHang;
    private String phuongThucThanhToan;

    // giỏ hàng -> đặt hàng
    private List<Integer> maGioHangList;

    // Đặt hàng trực tiếp ("Mua ngay"), bỏ qua giỏ hàng. Có giá trị -> ưu tiên dùng danh sách này.
    private List<DetailItemRequest> danhSachSanPham;

    @Data
    public static class DetailItemRequest {
        private Integer maBienThe; // mã ProductVariant
        private Integer soLuong;
    }
}