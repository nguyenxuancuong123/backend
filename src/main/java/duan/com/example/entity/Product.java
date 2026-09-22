package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "sanpham")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "masp", nullable = false)
    private Integer maSP;

    @Column(name = "tensp", nullable = false, length = 150)
    private String tenSP;

    @Column(name = "giaban", precision = 12, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "giakhuyenmai", precision = 12, scale = 2)
    private BigDecimal giaKhuyenMai;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "hinhanh", length = 255)
    private String hinhAnh;

    @Column(name = "ngaythem", insertable = false, updatable = false)
    private LocalDateTime ngayThem;

    @ManyToOne
    @JoinColumn(name = "madm")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariant> bienThes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> hinhAnhs;

}