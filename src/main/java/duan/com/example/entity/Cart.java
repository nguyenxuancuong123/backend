package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "giohang")
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "magiohang", nullable = false)
    private Integer maGioHang;

    @Column(name = "soluong")
    private Integer soLuong;

    @Column(name = "ngaythem", insertable = false, updatable = false)
    private LocalDateTime ngayThem;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private Users users;

    @ManyToOne
    @JoinColumn(name = "mabienthe", nullable = false)
    private ProductVariant productVariant;

}