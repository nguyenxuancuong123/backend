package duan.com.example.service.impl;

import duan.com.example.dto.request.CartRequest;
import duan.com.example.dto.response.CartResponse;
import duan.com.example.entity.*;
import duan.com.example.repository.CartRepository;
import duan.com.example.repository.ProductVariantRepository;
import duan.com.example.repository.UserRepository;
import duan.com.example.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Override
    public List<CartResponse> getGioHang(String email) {
        Users user = getUserByEmail(email);
        return cartRepository.findByUsers_Id(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartResponse themVaoGio(String email, CartRequest request) {
        Users user = getUserByEmail(email);

        ProductVariant bienThe = productVariantRepository.findById(request.getMaBienThe())
                .orElseThrow(() -> new EntityNotFoundException("Biến thể sản phẩm không tồn tại: " + request.getMaBienThe()));

        Integer soLuongThem = request.getSoLuong();
        if (soLuongThem == null || soLuongThem <= 0) {
            throw new IllegalArgumentException("Số lượng thêm vào giỏ phải lớn hơn 0");
        }

        Cart cart = cartRepository
                .findByUsers_IdAndProductVariant_MaBienThe(user.getId(), request.getMaBienThe())
                .orElse(null);

        if (cart != null) {
            // Đã có biến thể này trong giỏ -> cộng dồn số lượng
            int soLuongMoi = cart.getSoLuong() + soLuongThem;
            kiemTraTonKho(bienThe, soLuongMoi);
            cart.setSoLuong(soLuongMoi);
        } else {
            // Chưa có -> tạo mới
            kiemTraTonKho(bienThe, soLuongThem);
            cart = new Cart();
            cart.setUsers(user);
            cart.setProductVariant(bienThe);
            cart.setSoLuong(soLuongThem);
            cart.setNgayThem(LocalDateTime.now());
        }

        cart = cartRepository.save(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse capNhatSoLuong(String email, Integer magh, Integer soLuong) {
        Users user = getUserByEmail(email);
        Cart cart = layGioHangCuaUser(magh, user.getId());

        if (soLuong == null || soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        kiemTraTonKho(cart.getProductVariant(), soLuong);

        cart.setSoLuong(soLuong);
        cart = cartRepository.save(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public void xoaKhoiGio(String email, Integer magh) {
        Users user = getUserByEmail(email);
        Cart cart = layGioHangCuaUser(magh, user.getId());
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public void xoaToanBoGio(String email) {
        Users user = getUserByEmail(email);
        cartRepository.deleteByUsers_Id(user.getId());
    }


    private Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng: " + email));
    }

    private Cart layGioHangCuaUser(Integer magh, Integer userId) {
        Cart cart = cartRepository.findById(magh)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy mục trong giỏ hàng: " + magh));

        if (!cart.getUsers().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên giỏ hàng này");
        }
        return cart;
    }

    private void kiemTraTonKho(ProductVariant bienThe, int soLuongCan) {
        Integer tonKho = bienThe.getSoLuongTon();
        if (tonKho != null && soLuongCan > tonKho) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho (" + tonKho + ")");
        }
    }

    // Giá áp dụng: ưu tiên giá khuyến mãi ở cấp Product (nếu > 0), ngược lại lấy giá riêng của biến thể.
    // Đây là quy ước hợp lý dựa trên entity hiện có; đổi lại nếu logic khuyến mãi của bạn khác.
    public CartResponse toResponse(Cart cart) {
        ProductVariant bienThe = cart.getProductVariant();
        Product product = bienThe.getProduct(); // Lấy từ mối quan hệ 2 chiều

        // Tính giá thực tế (ưu tiên giá khuyến mãi nếu có)
        BigDecimal giaBanReal = (product.getGiaKhuyenMai() != null
                && product.getGiaKhuyenMai().compareTo(BigDecimal.ZERO) > 0)
                ? product.getGiaKhuyenMai()
                : product.getGiaBan();

        return CartResponse.builder()
                .maGioHang(cart.getMaGioHang())
                .maBienThe(bienThe.getMaBienThe())
                .maSP(product.getMaSP())
                .tenSP(product.getTenSP())
                .hinhAnh(product.getHinhAnh()) // <--- Gán tên file ảnh từ Product vào DTO
                .size(bienThe.getSize() != null ? bienThe.getSize().getTenSize() : "")
                .tenMau(bienThe.getColor() != null ? bienThe.getColor().getTenMau() : "")
                .giaBan(giaBanReal)
                .soLuong(cart.getSoLuong())
                .soLuongTon(bienThe.getSoLuongTon())
                .thanhTien(giaBanReal.multiply(BigDecimal.valueOf(cart.getSoLuong())))
                .ngayThem(cart.getNgayThem())
                .build();
    }
}
