package duan.com.example.service;

import duan.com.example.dto.request.ReviewRequest;
import duan.com.example.dto.response.ReviewResponse;
import duan.com.example.dto.response.ReviewSummaryResponse;
import org.springframework.data.domain.Page;

public interface ReviewService {

    // Gửi đánh giá (chỉ được nếu đã mua và đơn "Hoàn thành")
    ReviewResponse guiDanhGia(Integer maSP, ReviewRequest request, String email);

    // Lấy danh sách đánh giá của sản phẩm (phân trang)
    Page<ReviewResponse> layDanhSachDanhGia(Integer maSP, int page, int size);

    // Lấy TẤT CẢ đánh giá trong hệ thống (dành cho Admin, phân trang)
    Page<ReviewResponse> layTatCaDanhGia(int page, int size);

    // Lấy tóm tắt thống kê đánh giá
    ReviewSummaryResponse layTomTatDanhGia(Integer maSP);

    // Admin xóa đánh giá vi phạm
    void xoaDanhGia(Integer maDanhGia);
}

