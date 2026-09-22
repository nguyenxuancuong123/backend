package duan.com.example.controller;

import duan.com.example.dto.response.ProFileResponse;
import duan.com.example.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<ProFileResponse> getHoSo(
            @AuthenticationPrincipal UserDetails userDetails) {
        ProFileResponse response = userService.getHoSo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
