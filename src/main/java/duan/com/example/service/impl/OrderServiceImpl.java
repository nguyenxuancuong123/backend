package duan.com.example.service.impl;

import duan.com.example.dto.response.OrderDetailResponse;
import duan.com.example.dto.response.OrderResponse;
import duan.com.example.dto.request.CreateOrderRequest;
import duan.com.example.dto.request.UpdateStatusRequest;
import duan.com.example.entity.*;
import duan.com.example.repository.OrderDetailRepository;
import duan.com.example.repository.OrderRepository;
import duan.com.example.repository.CartRepository;
import duan.com.example.repository.ProductVariantRepository;
import duan.com.example.repository.UserRepository;
import duan.com.example.service.OrderService;
import duan.com.example.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
    private final NotificationService notificationService;

    private static final String STOCK_KEY_PREFIX = "product:stock:";
    private static final List<String> DANH_SACH_TRANG_THAI =
            List.of("Chờ duyệt", "Đang giao", "Hoàn thành", "Hủy");

    private Users layUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + email));
    }

    private OrderResponse toResponse(Order dh) {
        List<OrderDetailResponse> chiTiet = dh.getOrderDetails()
                .stream()
                .map(ct -> {
                    ProductVariant bt = ct.getProductVariant();
                    BigDecimal thanhTien = ct.getDonGia().multiply(BigDecimal.valueOf(ct.getSoLuong()));
                    return OrderDetailResponse.builder()
                            .maBienThe(bt.getMaBienThe())
                            .maSP(bt.getProduct().getMaSP())
                            .tenSP(bt.getProduct().getTenSP())
                            .hinhAnh(bt.getProduct().getHinhAnh())
                            .size(bt.getSize() != null ? bt.getSize().getTenSize() : null)
                            .tenMau(bt.getColor() != null ? bt.getColor().getTenMau() : null)
                            .soLuong(ct.getSoLuong())
                            .donGia(ct.getDonGia())
                            .thanhTien(thanhTien)
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .maDonHang(dh.getMaDonHang())
                .trangThai(dh.getTrangThai())
                .tongTien(dh.getTongTien())
                .ngayDat(dh.getNgayDat())
                .tenNguoiNhan(dh.getTenNguoiNhan())
                .soDienThoaiNhan(dh.getSoDienThoaiNhan())
                .diaChiGiaoHang(dh.getDiaChiGiaoHang())
                .phuongThucThanhToan(dh.getPhuongThucThanhToan())
                .chiTiet(chiTiet)
                .build();
    }

    // Chỉ khởi tạo counter Redis từ DB nếu chưa tồn tại — tránh mỗi lần gọi lại ghi đè
    // số lượng đã bị trừ tạm bởi các giao dịch đồng thời khác.
    private RAtomicLong layHoacKhoiTaoStockRedis(ProductVariant bienThe) {
        String key = STOCK_KEY_PREFIX + bienThe.getMaBienThe();
        RAtomicLong stock = redissonClient.getAtomicLong(key);
        if (!stock.isExists()) {
            Integer tonKho = bienThe.getSoLuongTon();
            stock.compareAndSet(0L, tonKho != null ? tonKho.longValue() : 0L);
        }
        return stock;
    }

    @Override
    @Transactional
    public OrderResponse taoDonHang(String email, CreateOrderRequest request) {
        Users user = layUser(email);

        // 1. Đặt hàng trực tiếp (không qua giỏ hàng)
        if (request.getDanhSachSanPham() != null && !request.getDanhSachSanPham().isEmpty()) {
            return taoDonHangTrucTiep(user, request);
        }

        // 2. Đặt hàng từ giỏ hàng
        List<Cart> danhSachGio;
        if (request.getMaGioHangList() == null || request.getMaGioHangList().isEmpty()) {
            danhSachGio = cartRepository.findByUsers_Id(user.getId());
        } else {
            danhSachGio = request.getMaGioHangList().stream()
                    .map(magh -> cartRepository.findById(magh)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy mục giỏ hàng: " + magh)))
                    .collect(Collectors.toList());
            danhSachGio.forEach(gh -> {
                if (!gh.getUsers().getId().equals(user.getId())) {
                    throw new RuntimeException("Bạn không có quyền đặt giỏ hàng này: " + gh.getMaGioHang());
                }
            });
        }
        if (danhSachGio.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng.");
        }

        // Tập hợp các biến thể đã trừ tồn kho trên Redis, để rollback nếu lỗi
        Map<Integer, Integer> daTruRedis = new HashMap<>();

        try {
            // Bước 1: Trừ tồn kho ATOMIC trên Redis
            for (Cart gh : danhSachGio) {
                ProductVariant bienThe = gh.getProductVariant();
                RAtomicLong stockRedis = layHoacKhoiTaoStockRedis(bienThe);

                long remainStock = stockRedis.addAndGet(-gh.getSoLuong());

                if (remainStock < 0) {
                    stockRedis.addAndGet(gh.getSoLuong());
                    throw new RuntimeException("Sản phẩm '" + bienThe.getProduct().getTenSP() + "' đã hết hàng!");
                }

                daTruRedis.put(bienThe.getMaBienThe(), gh.getSoLuong());
            }

            // Bước 2: Lưu đơn hàng vào CSDL
            return thucHienDatHang(user, danhSachGio, request);

        } catch (Exception e) {
            // Rollback Redis: cộng trả lại số lượng đã trừ nếu gặp lỗi
            daTruRedis.forEach((maBienThe, soLuong) -> {
                RAtomicLong stockRedis = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + maBienThe);
                stockRedis.addAndGet(soLuong);
            });
            throw new RuntimeException(e.getMessage());
        }
    }

    private OrderResponse thucHienDatHang(Users user, List<Cart> danhSachGio, CreateOrderRequest request) {
        BigDecimal tongTien = BigDecimal.ZERO;
        List<OrderDetail> chiTietList = new ArrayList<>();

        Order donHang = new Order();
        donHang.setUsers(user);
        donHang.setTongTien(BigDecimal.ZERO);
        donHang.setTrangThai("Chờ duyệt");
        donHang.setNgayDat(LocalDateTime.now());
        donHang.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
        donHang.setTenNguoiNhan(request.getTenNguoiNhan());
        donHang.setSoDienThoaiNhan(request.getSoDienThoaiNhan());
        donHang.setDiaChiGiaoHang(request.getDiaChiGiaoHang());
        Order savedDonHang = orderRepository.save(donHang);

        for (Cart gh : danhSachGio) {
            ProductVariant bienThe = productVariantRepository.findByIdWithLock(gh.getProductVariant().getMaBienThe())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại."));

            BigDecimal giaKhuyenMai = bienThe.getProduct().getGiaKhuyenMai();
            BigDecimal donGia = (giaKhuyenMai != null && giaKhuyenMai.compareTo(BigDecimal.ZERO) > 0)
                    ? giaKhuyenMai : bienThe.getGiaBan();
            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(gh.getSoLuong())));

            OrderDetail chiTiet = new OrderDetail();
            chiTiet.setDonHang(savedDonHang);
            chiTiet.setProductVariant(bienThe);
            chiTiet.setSoLuong(gh.getSoLuong());
            chiTiet.setDonGia(donGia);
            // thanhTien KHÔNG set thủ công — đây là cột GENERATED ALWAYS STORED, DB tự tính
            chiTietList.add(chiTiet);

            // Cập nhật tồn kho đồng bộ vào DB
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() - gh.getSoLuong());
            productVariantRepository.save(bienThe);
        }

        orderDetailRepository.saveAll(chiTietList);
        savedDonHang.setTongTien(tongTien);
        savedDonHang.setOrderDetails(chiTietList);
        orderRepository.save(savedDonHang);
        cartRepository.deleteAll(danhSachGio);

        return toResponse(savedDonHang);
    }

    // Đặt hàng ngay
    private OrderResponse taoDonHangTrucTiep(Users user, CreateOrderRequest request) {
        List<CreateOrderRequest.DetailItemRequest> danhSach = request.getDanhSachSanPham();
        Map<Integer, Integer> daTruRedis = new HashMap<>();

        try {
            // Bước 1: Trừ kho trên Redis trước
            for (CreateOrderRequest.DetailItemRequest item : danhSach) {
                if (item.getMaBienThe() == null || item.getSoLuong() == null || item.getSoLuong() <= 0) {
                    throw new RuntimeException("Dữ liệu sản phẩm không hợp lệ.");
                }

                ProductVariant bienThe = productVariantRepository.findById(item.getMaBienThe())
                        .orElseThrow(() -> new RuntimeException("Biến thể sản phẩm không tồn tại: " + item.getMaBienThe()));

                RAtomicLong stockRedis = layHoacKhoiTaoStockRedis(bienThe);
                long remainStock = stockRedis.addAndGet(-item.getSoLuong());

                if (remainStock < 0) {
                    stockRedis.addAndGet(item.getSoLuong());
                    throw new RuntimeException("Sản phẩm '" + bienThe.getProduct().getTenSP() + "' không đủ tồn kho!");
                }

                daTruRedis.put(item.getMaBienThe(), item.getSoLuong());
            }

            // Bước 2: Tạo đơn hàng & cập nhật DB
            return thucHienDatHangTrucTiep(user, danhSach, request);

        } catch (Exception e) {
            daTruRedis.forEach((maBienThe, soLuong) -> {
                RAtomicLong stockRedis = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + maBienThe);
                stockRedis.addAndGet(soLuong);
            });
            throw new RuntimeException(e.getMessage());
        }
    }

    private OrderResponse thucHienDatHangTrucTiep(Users user,
                                                  List<CreateOrderRequest.DetailItemRequest> danhSach,
                                                  CreateOrderRequest request) {
        BigDecimal tongTien = BigDecimal.ZERO;
        List<OrderDetail> chiTietList = new ArrayList<>();

        Order donHang = new Order();
        donHang.setUsers(user);
        donHang.setTongTien(BigDecimal.ZERO);
        donHang.setTrangThai("Chờ duyệt");
        donHang.setNgayDat(LocalDateTime.now());
        donHang.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
        donHang.setTenNguoiNhan(request.getTenNguoiNhan());
        donHang.setSoDienThoaiNhan(request.getSoDienThoaiNhan());
        donHang.setDiaChiGiaoHang(request.getDiaChiGiaoHang());
        Order savedDonHang = orderRepository.save(donHang);

        for (CreateOrderRequest.DetailItemRequest item : danhSach) {
            ProductVariant bienThe = productVariantRepository.findByIdWithLock(item.getMaBienThe())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + item.getMaBienThe()));

            BigDecimal giaKhuyenMai = bienThe.getProduct().getGiaKhuyenMai();
            BigDecimal donGia = (giaKhuyenMai != null && giaKhuyenMai.compareTo(BigDecimal.ZERO) > 0)
                    ? giaKhuyenMai : bienThe.getGiaBan();
            tongTien = tongTien.add(donGia.multiply(BigDecimal.valueOf(item.getSoLuong())));

            OrderDetail chiTiet = new OrderDetail();
            chiTiet.setDonHang(savedDonHang);
            chiTiet.setProductVariant(bienThe);
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setDonGia(donGia);
            chiTietList.add(chiTiet);

            bienThe.setSoLuongTon(bienThe.getSoLuongTon() - item.getSoLuong());
            productVariantRepository.save(bienThe);
        }

        orderDetailRepository.saveAll(chiTietList);
        savedDonHang.setTongTien(tongTien);
        savedDonHang.setOrderDetails(chiTietList);
        orderRepository.save(savedDonHang);

        return toResponse(savedDonHang);
    }

    @Override
    public List<OrderResponse> getDonHangCuaToi(String email) {
        Users user = layUser(email);
        return orderRepository.findByUsers_IdOrderByNgayDatDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getChiTietDonHang(String email, Integer maDonHang) {
        Users user = layUser(email);
        Order order = orderRepository.findByMaDonHangAndUsers_Id(maDonHang, user.getId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse huyDonHang(String email, Integer maDonHang) {
        Users user = layUser(email);
        Order order = orderRepository.findByMaDonHangAndUsers_Id(maDonHang, user.getId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));

        if (!"Chờ duyệt".equals(order.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ duyệt'.");
        }

        hoanTonKho(order);
        order.setTrangThai("Hủy");
        return toResponse(orderRepository.save(order));
    }

    @Override
    public Page<OrderResponse> getDonHangsPaginated(int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by("ngayDat").descending());
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse capNhatTrangThai(Integer maDonHang, UpdateStatusRequest request) {
        Order order = orderRepository.findById(maDonHang)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + maDonHang));

        String trangThaiMoi = request.getTrangThai();
        if (!DANH_SACH_TRANG_THAI.contains(trangThaiMoi)) {
            throw new RuntimeException("Trạng thái không hợp lệ.");
        }

        // Nếu admin hủy đơn đang "Chờ duyệt" -> hoàn lại tồn kho DB & Redis
        if ("Hủy".equals(trangThaiMoi) && "Chờ duyệt".equals(order.getTrangThai())) {
            hoanTonKho(order);
        }

        order.setTrangThai(trangThaiMoi);
        Order daLuu = orderRepository.save(order);


        Notification notification = notificationService.luuThongBao(daLuu);
        String email = daLuu.getUsers().getEmail();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.guiWebSocket(notification, email);
                }
            });
        } else {
            notificationService.guiWebSocket(notification, email);
        }

        return toResponse(daLuu);
    }

    private void hoanTonKho(Order order) {
        order.getOrderDetails().forEach(ct -> {
            Integer maBienThe = ct.getProductVariant().getMaBienThe();

            // Lấy lại biến thể từ DB kèm theo Lock để tránh race condition / lost update
            ProductVariant bienThe = productVariantRepository.findByIdWithLock(maBienThe)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy biến thể sản phẩm ID: " + maBienThe));

            // Cộng lại số lượng tồn kho ở DB
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() + ct.getSoLuong());
            productVariantRepository.save(bienThe);

            // Cập nhật tồn kho trên Redis
            RAtomicLong stockRedis = redissonClient.getAtomicLong(STOCK_KEY_PREFIX + maBienThe);
            if (stockRedis.isExists()) {
                stockRedis.addAndGet(ct.getSoLuong());
            }
        });
    }
}
