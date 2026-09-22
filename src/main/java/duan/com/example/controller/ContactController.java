package duan.com.example.controller;

import duan.com.example.entity.Contact;
import duan.com.example.entity.Users;
import duan.com.example.repository.ContactRepository;
import duan.com.example.repository.UserRepository;
import duan.com.example.utils.ChatRoleUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    // SimpMessagingTemplate :  dùng để gửi tin nhắn đến các client thông qua giao thức WebSocket
    private final SimpMessagingTemplate messagingTemplate;

    // Kênh chung: mọi Admin/Employee đang online subscribe kênh này để nhận thông báo
    public static final String STAFF_TOPIC = "/topic/staff-inbox";

    @MessageMapping("/chat.sendMessage")
    // Principal : đại diện cho người dùng khi đã được xác thực
    public void sendMessage(@Payload Contact contact, Principal principal) {
            if (principal == null) return;
        Authentication authentication = (Authentication) principal;
        String me = authentication.getName();
        boolean isStaff = ChatRoleUtil.isStaff(authentication);

        contact.setSender(me);
        contact.setTimestamp(LocalDateTime.now());
        contact.setId(null); // luôn tạo bản ghi mới cho mỗi tin nhắn lưu vào DB

        // Tìm thông tin người gửi để gán tên hiển thị
        var senderUserOpt = userRepository.findByEmail(me);
        String senderDisplayName = senderUserOpt.map(Users::getHoten).orElse(me);
        boolean isGuest = senderUserOpt.map(Users::isGuest).orElse(false);

        if (isStaff) {
            // Admin/Employee trả lời -> payload BẮT BUỘC có receiver = username khách hàng
            String customer = contact.getReceiver();
            if (!StringUtils.hasText(customer)) {
                throw new IllegalArgumentException("Thiếu receiver: chưa xác định trả lời cho khách hàng nào");
            }

            Contact saved = contactRepository.save(contact);
            saved.setSenderName(senderDisplayName);
            saved.setIsGuest(isGuest);

            messagingTemplate.convertAndSendToUser(customer, "/queue/messages", saved); // riêng cho khách hàng
            messagingTemplate.convertAndSendToUser(me, "/queue/messages", saved); // echo lại cho chính người vừa gửi
            messagingTemplate.convertAndSend(STAFF_TOPIC, saved); // báo các nhân viên khác cập nhật hộp thư
        } else {
            // Khách hàng gửi -> LUÔN vào hộp thư chung, thông tin receiver client gửi lên
            contact.setReceiver(Contact.STAFF_INBOX);

            Contact saved = contactRepository.save(contact);
            saved.setSenderName(senderDisplayName);
            saved.setIsGuest(isGuest);

            messagingTemplate.convertAndSendToUser(me, "/queue/messages", saved); // echo lại cho chính khách hàng
            messagingTemplate.convertAndSend(STAFF_TOPIC, saved); // báo real-time cho TẤT CẢ Admin/Employee đang online
        }
    }
}