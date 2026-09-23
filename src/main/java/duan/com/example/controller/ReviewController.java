package duan.com.example.controller;

import duan.com.example.dto.request.ReviewRequest;
import duan.com.example.dto.response.ReviewResponse;
import duan.com.example.dto.response.ReviewSummaryResponse;
import duan.com.example.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Gửi đánh giá — cần đăng nhập và đã mua hàng
    @PostMapping("/{masp}")
    public ResponseEntity<ReviewResponse> guiDanhGia(
            @PathVariable Integer masp,
            @RequestBody ReviewRequest request,
            Authentication authentication) {
        ReviewResponse response = reviewService.guiDanhGia(masp, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    // Lấy danh sách đánh giá của sản phẩm — public
    @GetMapping("/{masp}")
    public ResponseEntity<Page<ReviewResponse>> layDanhSachDanhGia(
            @PathVariable Integer masp,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.layDanhSachDanhGia(masp, page, size));
    }

    // Lấy tóm tắt thống kê đánh giá — public
    @GetMapping("/{masp}/summary")
    public ResponseEntity<ReviewSummaryResponse> layTomTatDanhGia(@PathVariable Integer masp) {
        return ResponseEntity.ok(reviewService.layTomTatDanhGia(masp));
    }

    // Admin xóa đánh giá vi phạm
    @DeleteMapping("/{madanhgia}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<Void> xoaDanhGia(@PathVariable Integer madanhgia) {
        reviewService.xoaDanhGia(madanhgia);
        return ResponseEntity.noContent().build();
    }
}
