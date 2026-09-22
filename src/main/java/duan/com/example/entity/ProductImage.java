package duan.com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "hinhanhsanpham")
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mahinhanh", nullable = false)
    private Integer maHinhAnh;

    @Column(name = "duongdan", nullable = false, length = 255)
    private String duongDan;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "masp", nullable = false)
    private Product product;

}