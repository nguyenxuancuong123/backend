package duan.com.example.config;

import duan.com.example.utils.ChatRoleUtil;
import duan.com.example.repository.BlacklistedTokenRepository;
import duan.com.example.service.JwtService;
import duan.com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
// WebSocketAuthChannelInterceptor : xác thực và phân quyền khi kết nối STOMP
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final UserService userService;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    // Kênh hộp thư chung chỉ dành cho Admin/Employee. Chặn lại để user khác không xem được
    private static final String STAFF_TOPIC = "/topic/staff-inbox";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            xacThucConnect(accessor);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                && STAFF_TOPIC.equals(accessor.getDestination())) {
            kiemTraQuyen(accessor);
        }
        return message;
    }

    private void xacThucConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new MessagingException("Thiếu hoặc sai định dạng Authorization header khi kết nối WebSocket");
        }

        String jwt = authHeader.substring(7);

        if (blacklistedTokenRepository.existsByToken(jwt)) {
            throw new MessagingException("Token đã bị thu hồi");
        }

        String userEmail = jwtService.extracUserName(jwt);
        if (!StringUtils.hasText(userEmail)) {
            throw new MessagingException("Token không hợp lệ");
        }

        // load thông tin user từ DB
        UserDetails userDetails = userService.userDetailService().loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            throw new MessagingException("Token không hợp lệ hoặc đã hết hạn");
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        accessor.setUser(authentication);
        // Spring lưu authentication này vào WebSocket session
        // Key = authentication.getName() = "user1@gmail.com"
        // → Từ đây Spring biết session này thuộc về "user1@gmail.com"
        // mỗi user vào sẽ có 1 id khác nhau để cho admin biết
    }

    private void kiemTraQuyen(StompHeaderAccessor accessor) {
        Object user = accessor.getUser();
        if (!(user instanceof Authentication authentication) || !ChatRoleUtil.isStaff(authentication)) {
            throw new MessagingException("Không có quyền xem hộp thư.");
        }
    }
}