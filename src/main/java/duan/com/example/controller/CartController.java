package duan.com.example.controller;

import duan.com.example.dto.request.CartRequest;
import duan.com.example.dto.response.CartResponse;
import duan.com.example.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Lấy toàn bộ giỏ hàng của user hiện tại
    @GetMapping
    public ResponseEntity<List<CartResponse>> getGioHang(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CartResponse> result = cartService.getGioHang(userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    // Thêm sản phẩm vào giỏ
    @PostMapping
    public ResponseEntity<CartResponse> themVaoGio(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CartRequest request) {
        CartResponse result = cartService.themVaoGio(userDetails.getUsername(), request);
        return ResponseEntity.ok(result);
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    @PutMapping("/{magh}")
    public ResponseEntity<CartResponse> capNhatSoLuong(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer magh,
            @RequestBody Map<String, Integer> body) {
        Integer soluong = body.get("soluong");
        if (soluong == null || soluong <= 0) {
            return ResponseEntity.badRequest().build();
        }
        CartResponse result = cartService.capNhatSoLuong(
                userDetails.getUsername(), magh, soluong);
        return ResponseEntity.ok(result);
    }

    // xóa 1 sp
    @DeleteMapping("/{magh}")
    public ResponseEntity<Void> xoaKhoiGio(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer magh) {
        cartService.xoaKhoiGio(userDetails.getUsername(), magh);
        return ResponseEntity.noContent().build();
    }

    // xóa toàn bộ giỏ hàng
    @DeleteMapping
    public ResponseEntity<Void> xoaToanBoGio(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.xoaToanBoGio(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
