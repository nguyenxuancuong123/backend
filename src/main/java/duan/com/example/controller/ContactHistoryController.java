package duan.com.example.controller;

import duan.com.example.entity.Contact;
import duan.com.example.entity.Users;
import duan.com.example.repository.ContactRepository;
import duan.com.example.repository.UserRepository;
import duan.com.example.utils.ChatRoleUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ContactHistoryController {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    // -> khách hàng gọi: luôn trả về đúng đoạn chat của CHÍNH họ
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) String partner,
            Principal principal,
            Authentication authentication
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chưa xác thực");
        }

        String me = principal.getName();
        boolean isStaff = ChatRoleUtil.isStaff(authentication);

        String customerUsername;
        if (isStaff) {
            if (!StringUtils.hasText(partner)) {
                return ResponseEntity.badRequest().body("Thiếu tham số partner (username khách hàng)");
            }
            customerUsername = partner;
        } else {
            // Khách hàng chỉ được xem đoạn chat của CHÍNH mình -> bỏ qua partner nếu client
            // lỡ gửi lên, tránh trường hợp 1 khách hàng dò xem lịch sử của khách hàng khác.
            customerUsername = me;
        }

        List<Contact> history = contactRepository
                .findBySenderOrReceiverOrderByTimestampAsc(customerUsername, customerUsername);

        return ResponseEntity.ok(history);
    }

    // GET /api/chat/conversations -> CHỈ Admin/Employee: danh sách khách hàng đã từng nhắn tin,
    // kèm tin nhắn cuối + thời gian, sắp mới nhất lên đầu (kiểu hộp thư inbox dùng chung cho
    // mọi nhân viên online).
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(Authentication authentication) {
        if (authentication == null || !ChatRoleUtil.isStaff(authentication)) {
            return ResponseEntity.status(403).body("Chỉ Admin/Employee được xem hộp thư chat");
        }

        List<Contact> all = contactRepository.findAllByOrderByTimestampAsc();

        Map<String, Contact> lastMessageByCustomer = new LinkedHashMap<>();
        for (Contact m : all) {
            String customer = Contact.STAFF_INBOX.equals(m.getReceiver()) ? m.getSender() : m.getReceiver();
            if (!StringUtils.hasText(customer)) continue;
            lastMessageByCustomer.put(customer, m);
        }

        List<ConversationSummary> result = lastMessageByCustomer.entrySet().stream()
                .map(e -> {
                    String custUsername = e.getKey();
                    var userOpt = userRepository.findByEmail(custUsername);
                    String custName = userOpt.map(Users::getHoten).orElse(custUsername);
                    Boolean isGuest = userOpt.map(Users::isGuest).orElse(false);

                    return new ConversationSummary(
                            custUsername,
                            custName,
                            isGuest,
                            e.getValue().getContent(),
                            e.getValue().getTimestamp());
                })
                .sorted(Comparator.comparing(ConversationSummary::lastTimestamp).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // DTO gọn cho danh sách hội thoại. Có thể dời sang package DTO nếu muốn đồng bộ style code.
    public record ConversationSummary(
            String customerUsername,
            String customerName,
            Boolean isGuest,
            String lastMessage,
            LocalDateTime lastTimestamp) {
    }
}