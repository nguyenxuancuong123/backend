package duan.com.example.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    // Một trong: "Chờ duyệt" | "Đang giao" | "Hoàn thành" | "Đã hủy"
    private String trangThai;
}