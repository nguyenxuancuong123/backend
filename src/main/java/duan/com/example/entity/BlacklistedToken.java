package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blacklisted_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Lưu chuỗi JWT (độ dài tối đa 512 ký tự)
    @Column(name = "token", length = 512, unique = true, nullable = false)
    private String token;
    // Thời điểm bị blacklist
    @Column(name = "blacklisted_at")
    private LocalDateTime blacklistedAt;
}
