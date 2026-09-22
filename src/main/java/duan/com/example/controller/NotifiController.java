package duan.com.example.controller;

import duan.com.example.dto.response.NotifiResponse;
import duan.com.example.entity.Notification;
import duan.com.example.repository.NotifiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifiController {
    private final NotifiRepository notifiRepository;

    @GetMapping
    public List<NotifiResponse> layLichSu(Authentication authentication) {
        String email = authentication.getName();
        return notifiRepository.findTop20ByUsers_EmailOrderByThoiGianDesc(email)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PatchMapping("/tick")
    public void danhDauDaDocHet(Authentication authentication) {
        notifiRepository.tickRead(authentication.getName());
    }

    private NotifiResponse toResponse(Notification t) {
        return new NotifiResponse(t.getMaThongBao(), t.getMaDonHang(), t.getTrangThai(), t.isDaDoc(), t.getThoiGian());
    }
}
