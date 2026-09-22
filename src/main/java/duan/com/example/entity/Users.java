package duan.com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import duan.com.example.entity.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "nguoidung")
@NoArgsConstructor
@AllArgsConstructor
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "hoten", nullable = false, length = 100)
    private String hoten;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "matkhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "sodienthoai", length = 20)
    private String soDienThoai;

    @Column(name = "diachi", length = 200)
    private String diaChi;

    @Enumerated(EnumType.STRING)
    @Column(name = "vaitro", length = 20)
    private Role vaiTro;

    @Column(name = "ngaytao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "trangthai")
    private Boolean trangThai;

    @Column(name = "is_guest")
    private Boolean isGuest = false;

    public boolean isGuest() {
        return Boolean.TRUE.equals(isGuest);
    }

    @JsonIgnore
    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Order> danhSachOrder;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL)
    private List<Cart> danhSachCart;


    // ----------------------- Authentication and Authorization ----------------------
    // Đây là những hàm nằm trong UserDetail

    // Trả về danh sách các quyền được cấp
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(vaiTro.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return matKhau;
    }

    @Override
    public String getUsername() {
        return email;
    }

    // ktra tài khoản có hết hạn ko
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // ktra tài khoản có bị khóa ko
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // ktra thông tin đăng nhập có hết hạn ko
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // kích hoạt tài khoản
    @Override
    public boolean isEnabled() {
        return true;
    }
}