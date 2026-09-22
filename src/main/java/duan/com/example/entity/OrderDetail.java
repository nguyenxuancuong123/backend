package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "chitietdonhang")
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machitiet", nullable = false)
    private Integer maChiTiet;

    @Column(name = "soluong", nullable = false)
    private Integer soLuong;

    @Column(name = "dongia", nullable = false, precision = 12, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanhtien", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal thanhTien; // Read-only do là cột GENERATED ALWAYS STORED

    @ManyToOne
    @JoinColumn(name = "madonhang", nullable = false)
    private Order donHang;

    @ManyToOne
    @JoinColumn(name = "mabienthe", nullable = false)
    private ProductVariant productVariant;
}