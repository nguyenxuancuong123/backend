package duan.com.example.repository;

import duan.com.example.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // Lấy danh sách review của 1 sản phẩm, sắp xếp mới nhất trước
    Page<Review> findByProduct_MaSPOrderByNgayTaoDesc(Integer maSP, Pageable pageable);

    // Kiểm tra user đã review sản phẩm này chưa
    boolean existsByProduct_MaSPAndUsers_Id(Integer maSP, Integer userId);

    // Kiểm tra user đã mua và hoàn thành đơn hàng chứa sản phẩm này chưa
    @Query("""
        SELECT COUNT(od) > 0
        FROM OrderDetail od
        WHERE od.donHang.users.id = :userId
          AND od.productVariant.product.maSP = :maSP
          AND od.donHang.trangThai = 'Hoàn thành'
    """)
    boolean daMuaVaHoanThanh(@Param("maSP") Integer maSP, @Param("userId") Integer userId);
}
