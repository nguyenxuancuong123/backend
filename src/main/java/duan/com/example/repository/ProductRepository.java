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

    // Lọc sản phẩm theo danh mục có phân trang
    @Query("SELECT s FROM Product s WHERE s.category.maDM = :madm")
    Page<Product> findByDM_Id(@Param("madm") Integer madm, Pageable pageable);
}
