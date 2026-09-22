package duan.com.example.repository;

import duan.com.example.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    // Khóa dòng ở DB khi trừ tồn kho lúc chốt đơn, tránh 2 giao dịch cùng trừ 1 lúc
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from ProductVariant b where b.maBienThe = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") Integer id);
}