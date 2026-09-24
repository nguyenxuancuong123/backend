package duan.com.example.controller;

import duan.com.example.dto.response.OrderResponse;
import duan.com.example.dto.response.ProFileResponse;
import duan.com.example.dto.request.UpdateStatusRequest;
import duan.com.example.dto.response.ReviewResponse;
import duan.com.example.entity.Category;
import duan.com.example.entity.role.Role;
import duan.com.example.entity.Product;
import duan.com.example.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('Admin')")
public class AdminController {

    @Autowired
    private final CategoryService categoryService;
    private final ProductService sanphamService;
    private final OrderService orderService;
    private final UserService userService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ProFileResponse> getHoSo(
            @AuthenticationPrincipal UserDetails userDetails) {
        ProFileResponse response = userService.getHoSo(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // hiển thị danh sách danh mục có trong DB
    @GetMapping("/category/listcategory")
    public List<Category> getAllDanhMucs() {
        return categoryService.getAllDanhMucs();
    }
    // tạo danh mục mới
    @PostMapping("/category/createcategory")
    public Category createDanhMuc(@RequestBody Category category) {
        return categoryService.saveDanhMuc(category);
    }
    // cập nhật danh mục
    @PutMapping("/category/{madm}")
    public ResponseEntity<Category> updateDanhMuc(@PathVariable Integer madm, @RequestBody Category categoryDetails) {
        return categoryService.getDanhMucById(madm).map(danhMuc -> {
            danhMuc.setTenDM(categoryDetails.getTenDM());
            danhMuc.setMoTa(categoryDetails.getMoTa());

            Category updatedCategory = categoryService.saveDanhMuc(danhMuc);
            return ResponseEntity.ok(updatedCategory);
        }).orElse(ResponseEntity.notFound().build());
    }
    // xóa 1 danh mục
    @DeleteMapping("/category/{madm}")
    public ResponseEntity<Void> deleteDanhMuc(@PathVariable Integer madm) {
        // isPresent() : dùng kiểm tra đối tượng có khác null ko
        if (categoryService.getDanhMucById(madm).isPresent()) {
            categoryService.deleteDanhMuc(madm);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    // tìm kiếm danh mục
    @GetMapping("/category/{madm}")
    public ResponseEntity<Category> getDanhMucById(@PathVariable Integer madm) {
        return categoryService.getDanhMucById(madm)
                .map(ResponseEntity::ok) // nhận đối tượng dc tìm thấy
                .orElse(ResponseEntity.notFound().build()); // ko tìm thấy trả về lỗi
    }
    // hiển thị danh sách sản phẩm có phân trang trong DB
    @GetMapping("/product/listproduct")
    public ResponseEntity<Page<Product>> getSanPhamsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sanphamService.getSanPhamsPaginated(page, size));
    }
    // tìm kiếm sản phẩm
    @GetMapping("/product/{masp}")
    public ResponseEntity<Product> getSanPhamById(@PathVariable Integer masp) {
        return sanphamService.getSanPhamById(masp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // lấy danh sách sản phẩm theo danh mục (có phân trang)
    @GetMapping("/product/category/{madm}")
    public ResponseEntity<Page<Product>> getSanPhamsByDanhMucPaginated(
            @PathVariable Integer madm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sanphamService.getSanPhamsByDanhMucPaginated(madm, page, size));
    }

    // tạo sản phẩm mới
    private final String UPLOAD_DIR = "images/";

    // tạo sản phẩm mới
    @PostMapping(value = "/product/createproduct", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createSanPham(
            @RequestPart("product") Product product,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            if (file != null && !file.isEmpty()) {
                String fileName = saveImage(file);
                product.setHinhAnh(fileName); // Lưu tên file vào Database
            }
            Product savedProduct = sanphamService.saveSanPham(product);
            return ResponseEntity.ok(savedProduct);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // cập nhật sản phẩm
    @PutMapping(value = "/product/{masp}", consumes = {"multipart/form-data"})
    public ResponseEntity<Product> updateSanPham(
            @PathVariable Integer masp,
            @RequestPart("sanPham") Product productDetails,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        return sanphamService.getSanPhamById(masp).map(sanPham -> {
            try {
                sanPham.setTenSP(productDetails.getTenSP());
                sanPham.setGiaBan(productDetails.getGiaBan());
                sanPham.setMoTa(productDetails.getMoTa());
                sanPham.setCategory(productDetails.getCategory());

                // Nếu người dùng có chọn ảnh mới thì mới cập nhật ảnh
                if (file != null && !file.isEmpty()) {
                    String fileName = saveImage(file);
                    sanPham.setHinhAnh(fileName);
                }

                Product updatedProduct = sanphamService.saveSanPham(sanPham);
                return ResponseEntity.ok(updatedProduct);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // Hàm hỗ trợ lưu file ảnh vào thư mục máy chủ
    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }
    // xóa sản phẩm mới
    @DeleteMapping("/product/{masp}")
    public ResponseEntity<Void> deleteSanPham(@PathVariable Integer masp) {
        if (sanphamService.getSanPhamById(masp).isPresent()) {
            sanphamService.deleteSanPham(masp);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // xem danh sách các đơn hàng có phân trang
    @GetMapping("/order/list")
    public ResponseEntity<Page<OrderResponse>> getDonHangsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getDonHangsPaginated(page, size));
    }

    // cập nhật trạng thái đơn hàng
    @PutMapping("/order/{madh}/status")
    public ResponseEntity<OrderResponse> capNhatTrangThai(
            @PathVariable Integer madh,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(orderService.capNhatTrangThai(madh, request));
    }

    // Xem danh sách user có phân trang
    @GetMapping("/user")
    public ResponseEntity<Page<ProFileResponse>> getUsersPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsersPaginated(page, size));
    }

    // Xem chi tiết một user theo ID
    @GetMapping("/user/{id}")
    public ResponseEntity<ProFileResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Cập nhật vai trò của user
    @PutMapping("/user/{id}/role")
    public ResponseEntity<ProFileResponse> updateUserRole(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        Role newRole = Role.valueOf(request.get("vaiTro"));
        return ResponseEntity.ok(userService.updateUserRole(id, newRole));
    }

    // Cập nhật trạng thái (khoá/mở khoá) tài khoản user
    @PutMapping("/user/{id}/status")
    public ResponseEntity<ProFileResponse> updateUserStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> request) {
        Boolean newStatus = request.get("trangThai");
        return ResponseEntity.ok(userService.updateUserStatus(id, newStatus));
    }

    // Xem danh sách tất cả đánh giá của khách hàng (phân trang)
    @GetMapping("/review")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.layTatCaDanhGia(page, size));
    }

    // Xóa đánh giá vi phạm (Admin có quyền xóa)
    @DeleteMapping("/review/{maDanhGia}")
    public ResponseEntity<String> deleteReview(@PathVariable Integer maDanhGia) {
        reviewService.xoaDanhGia(maDanhGia);
        return ResponseEntity.ok("Đã xóa đánh giá thành công.");
    }
}