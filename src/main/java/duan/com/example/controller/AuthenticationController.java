package duan.com.example.controller;

import duan.com.example.dto.response.JwtAuthenticationResponse;
import duan.com.example.dto.request.RefreshTokenRequest;
import duan.com.example.dto.request.SigninRequest;
import duan.com.example.dto.request.SignUpRequest;
import duan.com.example.entity.Users;
import duan.com.example.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<Users> signup(@RequestBody SignUpRequest signUpRequest){
        return ResponseEntity.ok(authenticationService.Signup(signUpRequest) );
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest signinRequest){
        return ResponseEntity.ok(authenticationService.Signin(signinRequest) );
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest){
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Lấy token từ Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
            return ResponseEntity.ok("Đăng xuất thành công");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy token");
    }

    // Tạo hoặc lấy phiên tài khoản ảo cho khách truy cập lần đầu
    @PostMapping("/guest")
    public ResponseEntity<JwtAuthenticationResponse> guest() {
        return ResponseEntity.ok(authenticationService.loginAsGuest());
    }

    // Gộp dữ liệu tài khoản khách (giỏ hàng, tin nhắn) vào tài khoản thật sau khi login/signup
    @PostMapping("/merge-guest")
    public ResponseEntity<?> mergeGuest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập tài khoản chính thức"));
        }
        String guestToken = body.get("guestToken");
        if (!StringUtils.hasText(guestToken)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu guestToken trong request body"));
        }
        try {
            Map<String, Object> result = authenticationService.mergeGuest(userDetails.getUsername(), guestToken);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}