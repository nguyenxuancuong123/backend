package duan.com.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResponse {

    private BigDecimal diemTrungBinh;  // avgRating
    private long tongSoLuot;           // tổng lượt đánh giá
    private long soSao5;
    private long soSao4;
    private long soSao3;
    private long soSao2;
    private long soSao1;
}

