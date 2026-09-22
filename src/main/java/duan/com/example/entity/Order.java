package duan.com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "donhang")
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madonhang", nullable = false)
    private Integer maDonHang;

    @Column(name = "tennguoinhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @Column(name = "sodienthoainhan", nullable = false, length = 20)
    private String soDienThoaiNhan;

    @Column(name = "diachigiaohang", nullable = false, length = 255)
    private String diaChiGiaoHang;

    @Column(name = "phuongthucthanhtoan", length = 50)
    private String phuongThucThanhToan;

    @Column(name = "ngaydat", insertable = false, updatable = false)
    private LocalDateTime ngayDat;

    @Column(name = "tongtien", precision = 12, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "trangthai", length = 50)
    private String trangThai;

    @Column(name = "momo_order_id", length = 50)
    private String momoOrderId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private Users users;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

}