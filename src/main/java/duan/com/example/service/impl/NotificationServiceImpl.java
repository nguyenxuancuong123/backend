package duan.com.example.service.impl;

import duan.com.example.dto.response.OrderStatus;
import duan.com.example.entity.Order;
import duan.com.example.entity.Notification;
import duan.com.example.repository.NotifiRepository;
import duan.com.example.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    // SimpMessagingTemplate: gửi chat từ client qua server thông Websocket
    private final SimpMessagingTemplate messagingTemplate;
    private final NotifiRepository notifiRepository;

    @Override
    public Notification luuThongBao(Order donHang) {

        Notification notification = new Notification();
        notification.setMaDonHang(donHang.getMaDonHang());
        notification.setTrangThai(donHang.getTrangThai());
        notification.setDaDoc(false);
        notification.setThoiGian(LocalDateTime.now());
        notification.setUsers(donHang.getUsers());
        return notifiRepository.save(notification);
    }

    @Override
    public void guiWebSocket(Notification notification, String email) {
        try {
            OrderStatus payload = new OrderStatus(
                    notification.getMaThongBao(), notification.getMaDonHang(), notification.getTrangThai());
            messagingTemplate.convertAndSendToUser(email, "/queue/donhang-update", payload);
        } catch (Exception e) {
            log.error("Gui thong bao WebSocket that bai cho don hang {}: {}",
                    notification.getMaDonHang(), e.getMessage(), e);
        }
    }
}