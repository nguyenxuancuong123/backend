package duan.com.example.service;

import duan.com.example.dto.request.CartRequest;
import duan.com.example.dto.response.CartResponse;

import java.util.List;

public interface CartService {

    List<CartResponse> getGioHang(String email);

    CartResponse themVaoGio(String email, CartRequest request);

    CartResponse capNhatSoLuong(String email, Integer magh, Integer soLuong);

    void xoaKhoiGio(String email, Integer magh);

    void xoaToanBoGio(String email);
}