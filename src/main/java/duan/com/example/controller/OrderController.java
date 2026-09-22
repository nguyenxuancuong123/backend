package duan.com.example.controller;

import duan.com.example.dto.response.OrderResponse;
import duan.com.example.dto.request.CreateOrderRequest;
import duan.com.example.entity.Users;
import duan.com.example.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> taoDonHang(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) CreateOrderRequest request) {
        if (userDetails instanceof Users users && users.isGuest()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "GUEST_CHECKOUT_RESTRICTED",
                    "message", "Tài khoản khách không thể thanh toán. Vui lòng đăng nhập hoặc đăng ký tài khoản!"
            ));
        }
        if (request == null) request = new CreateOrderRequest();
        OrderResponse result = orderService.taoDonHang(userDetails.getUsername(), request);
        return ResponseEntity.ok(result);
    }

    // lấy danh sách đơn hàng của user hiện tại
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getDonHangCuaToi(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getDonHangCuaToi(userDetails.getUsername()));
    }

   // xem chi tiết đơn hàng
    @GetMapping("/{madh}")
    public ResponseEntity<OrderResponse> getChiTietDonHang(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer madh) {
        return ResponseEntity.ok(orderService.getChiTietDonHang(userDetails.getUsername(), madh));
    }

    // User hủy đơn hàng (trạng thái Chờ duyệt = 0)
    @PatchMapping("/{madh}/cancel")
    public ResponseEntity<OrderResponse> huyDonHang(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer madh) {
        return ResponseEntity.ok(orderService.huyDonHang(userDetails.getUsername(), madh));
    }


}
