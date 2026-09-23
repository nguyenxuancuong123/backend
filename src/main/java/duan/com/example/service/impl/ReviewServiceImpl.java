package duan.com.example.service.impl;

import duan.com.example.dto.request.ReviewRequest;
import duan.com.example.dto.response.ReviewResponse;
import duan.com.example.dto.response.ReviewSummaryResponse;
import duan.com.example.entity.Product;
import duan.com.example.entity.Review;
import duan.com.example.entity.Users;
import duan.com.example.repository.ProductRepository;
import duan.com.example.repository.ReviewRepository;
import duan.com.example.repository.UserRepository;
import duan.com.example.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse guiDanhGia(Integer maSP, ReviewRequest request, String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Product product = productRepository.findById(maSP)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        // Kiểm tra đã mua và hoàn thành chưa
        if (!reviewRepository.daMuaVaHoanThanh(maSP, user.getId())) {
            throw new IllegalStateException("Bạn chỉ có thể đánh giá sản phẩm đã mua và đơn hàng đã hoàn thành");
        }

        // Kiểm tra đã review chưa
        if (reviewRepository.existsByProduct_MaSPAndUsers_Id(maSP, user.getId())) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Validate số điểm
        if (request.getSoDiem() == null || request.getSoDiem() < 1 || request.getSoDiem() > 5) {
            throw new IllegalArgumentException("Số điểm phải từ 1 đến 5");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUsers(user);
        review.setSoDiem(request.getSoDiem());
        review.setBinhLuan(request.getBinhLuan());

        Review saved = reviewRepository.save(review);
        // Trigger PostgreSQL sẽ tự cập nhật diem_danh_gia_tb và so_luot_danh_gia

        return toResponse(saved);
    }

    @Override
    public Page<ReviewResponse> layDanhSachDanhGia(Integer maSP, int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        return reviewRepository
                .findByProduct_MaSPOrderByNgayTaoDesc(maSP, PageRequest.of(pageIndex, size))
                .map(this::toResponse);
    }

    @Override
    public Page<ReviewResponse> layTatCaDanhGia(int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        // Lấy tất cả và sắp xếp mới nhất lên trước
        return reviewRepository
                .findAll(PageRequest.of(pageIndex, size, org.springframework.data.domain.Sort.by("ngayTao").descending()))
                .map(this::toResponse);
    }

    @Override
    public ReviewSummaryResponse layTomTatDanhGia(Integer maSP) {
        List<Review> reviews = reviewRepository
                .findByProduct_MaSPOrderByNgayTaoDesc(maSP, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        if (reviews.isEmpty()) {
            return new ReviewSummaryResponse(BigDecimal.ZERO, 0, 0, 0, 0, 0, 0);
        }

        // Đếm số sao từng loại
        Map<Short, Long> countByStar = reviews.stream()
                .collect(Collectors.groupingBy(Review::getSoDiem, Collectors.counting()));

        double avg = reviews.stream()
                .mapToInt(r -> r.getSoDiem().intValue())
                .average()
                .orElse(0.0);

        return new ReviewSummaryResponse(
                BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP),
                reviews.size(),
                countByStar.getOrDefault((short) 5, 0L),
                countByStar.getOrDefault((short) 4, 0L),
                countByStar.getOrDefault((short) 3, 0L),
                countByStar.getOrDefault((short) 2, 0L),
                countByStar.getOrDefault((short) 1, 0L)
        );
    }

    @Override
    @Transactional
    public void xoaDanhGia(Integer maDanhGia) {
        if (!reviewRepository.existsById(maDanhGia)) {
            throw new IllegalArgumentException("Không tìm thấy đánh giá với mã: " + maDanhGia);
        }
        reviewRepository.deleteById(maDanhGia);
        // Trigger PostgreSQL sẽ tự cập nhật lại rating sau khi xóa
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getMaDanhGia(),
                review.getProduct().getMaSP(),
                review.getProduct().getTenSP(),
                review.getUsers().getEmail(),
                review.getUsers().getHoten(),
                review.getSoDiem(),
                review.getBinhLuan(),
                review.getNgayTao()
        );
    }
}
