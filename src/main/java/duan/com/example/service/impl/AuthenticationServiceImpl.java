package duan.com.example.service.impl;

import duan.com.example.dto.response.JwtAuthenticationResponse;
import duan.com.example.dto.request.RefreshTokenRequest;
import duan.com.example.dto.request.SigninRequest;
import duan.com.example.dto.request.SignUpRequest;
import duan.com.example.entity.*;
import duan.com.example.entity.role.Role;
import duan.com.example.repository.BlacklistedTokenRepository;
import duan.com.example.repository.UserRepository;
import duan.com.example.service.AuthenticationService;
import duan.com.example.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import duan.com.example.repository.CartRepository;
import duan.com.example.repository.ContactRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final CartRepository cartRepository;
    private final ContactRepository contactRepository;

    // Đăng ký
    public Users Signup(SignUpRequest signUpRequest){
        Users user = new Users();

        user.setEmail(signUpRequest.getEmail());
        user.setHoten(signUpRequest.getHoTen());
        user.setVaiTro((Role.User));
        user.setMatKhau(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setDiaChi(signUpRequest.getDiaChi());
        user.setSoDienThoai(signUpRequest.getSdt());

        return userRepository.save(user);
    }

    // Đăng nhập
    // UsernamePasswordAuthenticationToken : lưu tạm thông tin đăng nhập của người dùng
    public JwtAuthenticationResponse Signin(SigninRequest signinRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                (signinRequest.getEmail(),
                        signinRequest.getPassword()));

        var user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(()->new IllegalArgumentException("Invalid email or password"));
        var jwt = jwtService.generateToken(user);
        // dùng Map đề mở rộng thêm nếu user cần mở rộng thêm thông tin vào payload token
        var refreshToken = jwtService.generaRefreshToken(new HashMap<>(), user);

        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        jwtAuthenticationResponse.setHoTen(user.getHoten());
        jwtAuthenticationResponse.setIsGuest(user.isGuest());

        return jwtAuthenticationResponse;
    }

    // Refresh Token
    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest){
        String userEmail = jwtService.extracUserName(refreshTokenRequest.getToken());
        Users user = userRepository.findByEmail(userEmail).orElseThrow();
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)){
            var jwt = jwtService.generateToken(user);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();

            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
            jwtAuthenticationResponse.setHoTen(user.getHoten());
            jwtAuthenticationResponse.setIsGuest(user.isGuest());
            return jwtAuthenticationResponse;
        }
        return null;
    }

    @Override
    public void logout(String token) {
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setBlacklistedAt(LocalDateTime.now());
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    public JwtAuthenticationResponse loginAsGuest() {
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        String guestName = "Khách " + randomSuffix;
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String guestEmail = "guest_" + randomSuffix + "_" + uniqueId + "@guest.local";

        Users guest = new Users();
        guest.setHoten(guestName);
        guest.setEmail(guestEmail);
        guest.setMatKhau(passwordEncoder.encode(UUID.randomUUID().toString()));
        guest.setVaiTro(Role.User);
        guest.setIsGuest(true);
        guest.setTrangThai(true);
        guest.setSoDienThoai(null);
        guest.setDiaChi(null);

        Users savedGuest = userRepository.save(guest);

        String jwt = jwtService.generateToken(savedGuest);
        String refreshToken = jwtService.generaRefreshToken(new HashMap<>(), savedGuest);

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setToken(jwt);
        response.setRefreshToken(refreshToken);
        response.setHoTen(savedGuest.getHoten());
        response.setIsGuest(true);

        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> mergeGuest(String realUserEmail, String guestToken) {
        if (!StringUtils.hasText(guestToken)) {
            throw new IllegalArgumentException("guestToken không hợp lệ");
        }

        String guestEmail;
        try {
            guestEmail = jwtService.extracUserName(guestToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Token khách ảo không hợp lệ hoặc đã hết hạn");
        }

        if (guestEmail == null || guestEmail.equals(realUserEmail)) {
            return Map.of("message", "Không cần gộp tài khoản", "mergedCartItems", 0, "mergedMessages", 0);
        }

        Users guestUser = userRepository.findByEmail(guestEmail).orElse(null);
        if (guestUser == null || !guestUser.isGuest()) {
            return Map.of("message", "Tài khoản cần gộp không phải tài khoản khách", "mergedCartItems", 0, "mergedMessages", 0);
        }

        Users realUser = userRepository.findByEmail(realUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản thật: " + realUserEmail));

        // 1. Merge Giỏ hàng (Cart)
        List<Cart> guestCart = cartRepository.findByUsers_Id(guestUser.getId());
        List<Cart> realCart = cartRepository.findByUsers_Id(realUser.getId());
        int mergedCartCount = 0;

        for (Cart guestItem : guestCart) {
            Integer guestBienTheId = guestItem.getProductVariant().getMaBienThe();
            Optional<Cart> existingRealItem = realCart.stream()
                    .filter(rc -> rc.getProductVariant().getMaBienThe().equals(guestBienTheId))
                    .findFirst();

            if (existingRealItem.isPresent()) {
                Cart realItem = existingRealItem.get();
                realItem.setSoLuong(realItem.getSoLuong() + guestItem.getSoLuong());
                cartRepository.save(realItem);
                cartRepository.delete(guestItem);
            } else {
                guestItem.setUsers(realUser);
                cartRepository.save(guestItem);
            }
            mergedCartCount++;
        }

        // 2. Merge Lịch sử Chat (Contact)
        int updatedSenders = contactRepository.updateSender(guestEmail, realUserEmail);
        int updatedReceivers = contactRepository.updateReceiver(guestEmail, realUserEmail);
        int mergedMessages = updatedSenders + updatedReceivers;

        // 3. Vô hiệu hóa tài khoản khách và thu hồi token khách
        guestUser.setTrangThai(false);
        userRepository.save(guestUser);
        logout(guestToken);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Gộp dữ liệu tài khoản khách thành công");
        result.put("mergedCartItems", mergedCartCount);
        result.put("mergedMessages", mergedMessages);
        result.put("realUserEmail", realUserEmail);
        return result;
    }
}
