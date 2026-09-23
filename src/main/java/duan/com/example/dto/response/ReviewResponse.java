package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Integer maDanhGia;
    private Integer maSP;
    private String tenSP;
    private String email;
    private String hoTen;         // Tên người đánh giá
    private Short soDiem;
    private String binhLuan;
    private LocalDateTime ngayTao;
}

