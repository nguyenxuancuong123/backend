package duan.com.example.config;

import duan.com.example.entity.Users;
import duan.com.example.entity.role.Role;
import duan.com.example.repository.UserRepository;
import duan.com.example.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Oauth2Authentication implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String hoTen = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/oauth2/redirect?error=no_email_from_provider");
            return;
        }

        Users user = userRepository.findByEmail(email).orElseGet(() -> {
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setHoten(hoTen != null ? hoTen : email);
            newUser.setVaiTro(Role.User);
            newUser.setMatKhau(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setTrangThai(true);
            return userRepository.save(newUser);
        });

        // 1. Sinh Access Token
        String jwt = jwtService.generateToken(user);

        // 2. Sinh Refresh Token bằng hàm generaRefreshToken
        // Truyền một HashMap rỗng cho tham số extracClaim.
        String refreshToken = jwtService.generaRefreshToken(new HashMap<>(), user);

        // 3. Gửi cả 2 token về Frontend qua URL
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", jwt)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
