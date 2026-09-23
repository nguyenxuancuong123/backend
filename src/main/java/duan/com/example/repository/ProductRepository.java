package duan.com.example.repository;

import duan.com.example.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Lọc sản phẩm theo danh mục
    @Query("SELECT s FROM Product s WHERE s.category.maDM = :madm")
    List<Product> findByDM_Id(@Param("madm") Integer madm);

    // Lọc sản phẩm theo danh mục có phân trang, sắp xếp theo rating giảm dần
    @Query("SELECT s FROM Product s WHERE s.category.maDM = :madm ORDER BY s.diemDanhGiaTb DESC, s.maSP DESC")
    Page<Product> findByDM_Id(@Param("madm") Integer madm, Pageable pageable);

    // Tìm kiếm sản phẩm theo từ khóa (dùng ILIKE tận dụng GIN trigram index trong DB)
    // Sắp xếp theo rating giảm dần
    @Query(value = """
        SELECT * FROM sanpham
        WHERE tensp ILIKE '%' || :keyword || '%'
        ORDER BY diem_danh_gia_tb DESC, masp DESC
        """,
        countQuery = "SELECT COUNT(*) FROM sanpham WHERE tensp ILIKE '%' || :keyword || '%'",
        nativeQuery = true)
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Lấy tất cả sản phẩm phân trang, sắp xếp rating giảm dần
    @Query("SELECT s FROM Product s ORDER BY s.diemDanhGiaTb DESC, s.maSP DESC")
    Page<Product> findAllOrderByRatingDesc(Pageable pageable);
}
