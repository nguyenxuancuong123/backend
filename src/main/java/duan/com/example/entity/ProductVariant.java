package duan.com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "sanphambienthe")
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mabienthe", nullable = false)
    private Integer maBienThe;

    @Column(name = "soluongton")
    private Integer soLuongTon;

    @Column(name = "giaban", precision = 12, scale = 2)
    private BigDecimal giaBan;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "masp", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "masize", nullable = false)
    private Size size;

    @ManyToOne
    @JoinColumn(name = "mamau", nullable = false)
    private Color color;
}