package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "thongbao")
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mathongbao", nullable = false)
    private Integer maThongBao;

    @Column(name = "madonhang", nullable = false)
    private Integer maDonHang;

    @Column(name = "trangthai", length = 50, nullable = false)
    private String trangThai;

    @Column(name = "dadoc", nullable = false)
    private boolean daDoc = false;

    @Column(name = "thoigian", nullable = false)
    private LocalDateTime thoiGian;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private Users users;
}

