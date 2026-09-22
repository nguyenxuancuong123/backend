package duan.com.example.repository;

import duan.com.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUsers_IdOrderByNgayDatDesc(Integer nguoiDungId);

    Optional<Order> findByMaDonHangAndUsers_Id(Integer maDonHang, Integer nguoiDungId);

    Optional<Order> findByMomoOrderId(String momo_order_id);
}