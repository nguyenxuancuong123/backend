package duan.com.example.controller;


import duan.com.example.dto.response.MomoCreatePaymentResponse;
import duan.com.example.dto.request.MomoIpnRequest;
import duan.com.example.entity.Users;
import duan.com.example.service.MomoPaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/momo")
@RequiredArgsConstructor
public class MomoPaymentController {

    private final MomoPaymentService momoPaymentService;

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreatePaymentBody body) {
        if (userDetails instanceof Users users && users.isGuest()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "GUEST_CHECKOUT_RESTRICTED",
                    "message", "Tài khoản khách không thể thanh toán MoMo. Vui lòng đăng nhập hoặc đăng ký tài khoản!"
            ));
        }
        MomoCreatePaymentResponse response = momoPaymentService.createPayment(
                body.getAmount(), body.getOrderInfo(), body.getMadh());
        return ResponseEntity.ok(response);
    }

    // Endpoint server-to-server MoMo gọi vào sau khi giao dịch hoàn tất.
    // KHÔNG có Authorization header (MoMo gọi thẳng, không qua đăng nhập của app),
    // nên endpoint này phải được permitAll() trong SecurityConfig.
    @PostMapping("/ipn")
    public ResponseEntity<Void> ipn(@RequestBody MomoIpnRequest ipnRequest) {
        momoPaymentService.handleIpn(ipnRequest);
        // MoMo chỉ cần nhận HTTP 200 để biết đã nhận IPN thành công, không cần body.
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CreatePaymentBody {
        private long amount;
        private String orderInfo;
        private Integer madh; // mã đơn hàng nội bộ đã tạo trước đó (POST /api/v1/donhang)
    }
}