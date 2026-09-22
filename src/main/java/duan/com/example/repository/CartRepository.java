package duan.com.example.repository;

import duan.com.example.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    List<Cart> findByUsers_Id(Integer nguoiDungId);

    Optional<Cart> findByUsers_IdAndProductVariant_MaBienThe(Integer nguoiDungId, Integer maBienThe);

    void deleteByUsers_Id(Integer nguoiDungId);
}