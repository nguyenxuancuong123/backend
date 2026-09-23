package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "danhgia")
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madanhgia")
    private Integer maDanhGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "masp", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoidung", nullable = false)
    private Users users;

    @Column(name = "sodiem", nullable = false)
    private Short soDiem;

    @Column(name = "binhluan", columnDefinition = "TEXT")
    private String binhLuan;

    @Column(name = "ngaytao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}

