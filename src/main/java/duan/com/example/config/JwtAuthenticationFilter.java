package duan.com.example.config;

import duan.com.example.repository.BlacklistedTokenRepository;
import duan.com.example.service.JwtService;
import duan.com.example.service.UserService;
import ch.qos.logback.core.util.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor

// OncePerRequestFilter : cho phép lọc 1 lần duy nhất cho các request HTTP
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserService userService;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    // HttpServletRequest (Java Servlet API): đại diện HTTP gửi từ client lên server
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // kiểm tra có bị null và bắt đầu bằng tiền tố "Bearer" hay ko
        if (StringUtil.isNullOrEmpty(authHeader)||!org.apache.commons.lang3.StringUtils.startsWith(authHeader,"Bearer")){
            // nếu bị -> bỏ qua và chuyển qua bộ lọc ở bên khác mà ko thực hieện xác thực
            filterChain.doFilter(request,response);
            return;
        }

        // cắt bỏ 7 ký tự đầu -> lấy ra chuỗi JWT
        jwt = authHeader.substring(7);

        // Logout
        if (blacklistedTokenRepository.existsByToken(jwt)) {
            // Chặn request, trả về lỗi 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Token đã hết hạn hoặc đã đăng xuất!\"}");
            return; // Dừng lại, không cho request đi tiếp
        }

        // giải mã token và lấy ra thông tin login
        userEmail = jwtService.extracUserName(jwt);

        if (!StringUtil.isNullOrEmpty(userEmail)&& SecurityContextHolder.getContext().getAuthentication() == null) {
            // lấy thông tin user từ data lên
            UserDetails userDetails = userService.userDetailService().loadUserByUsername(userEmail);

            // kiểm tra user  đó đã hết hạn token hay chưa
            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }

        filterChain.doFilter(request,response);
    }
}
