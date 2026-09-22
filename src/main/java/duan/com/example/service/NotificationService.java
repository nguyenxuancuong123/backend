package duan.com.example.service;

import duan.com.example.entity.Order;
import duan.com.example.entity.Notification;

public interface NotificationService {
    Notification luuThongBao(Order order);

    void guiWebSocket(Notification notification, String email);
}
