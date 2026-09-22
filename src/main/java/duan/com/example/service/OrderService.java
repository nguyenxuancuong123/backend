package duan.com.example.service;

import duan.com.example.dto.response.OrderResponse;
import duan.com.example.dto.request.CreateOrderRequest;
import duan.com.example.dto.request.UpdateStatusRequest;

import java.util.List;

public interface OrderService {

    OrderResponse taoDonHang(String email, CreateOrderRequest request);

    List<OrderResponse> getDonHangCuaToi(String email);

    OrderResponse getChiTietDonHang(String email, Integer maDonHang);

    OrderResponse huyDonHang(String email, Integer maDonHang);

    List<OrderResponse> getAllDonHang();

    OrderResponse capNhatTrangThai(Integer maDonHang, UpdateStatusRequest request);
}