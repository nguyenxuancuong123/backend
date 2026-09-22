package duan.com.example.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // dùng để bật tính năng xử lý thông điệp
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // xử lý logic cho WebSocket
    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;

    // đăng ký endpoint kết nối
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // khai báo đường dẫn URL cho Vue gọi đến (Handshake)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:5174");
    }

    // cấu hình định tuyến tin nhắn
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // chạy ngầm 1 bộ tin nhắn trong RAM
        // -> /topic : dùng cho Pub/Sub
        // -> /queue : dùng cho Point - to - Point
        registry.enableSimpleBroker("/topic", "/queue");

        // mỗi tin nhắn Client gửi lên Server đi qua /app
        registry.setApplicationDestinationPrefixes("/app");

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Interceptor này chạy cho MỌI frame STOMP gửi lên, nhưng chỉ xử lý xác thực
        // ở bước CONNECT (xem chi tiết trong WebSocketAuthChannelInterceptor).
        registration.interceptors((ChannelInterceptor) webSocketAuthChannelInterceptor);
    }
}