package duan.com.example.controller;

import duan.com.example.entity.Product;
import duan.com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService sanphamService;

    // Lấy danh sách sản phẩm phân trang — mặc định sắp xếp theo rating giảm dần
    @GetMapping("/page")
    public ResponseEntity<Page<Product>> getSanPhamsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sanphamService.getSanPhamsPaginated(page, size));
    }

    // Tìm kiếm sản phẩm theo từ khóa — phân trang, sắp xếp theo rating giảm dần
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchSanPham(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(sanphamService.searchByKeyword(keyword, page, size));
    }

    // Lấy sản phẩm theo (id)
    @GetMapping("/{masp}")
    public ResponseEntity<Product> getSanPhamById(@PathVariable Integer masp) {
        return sanphamService.getSanPhamById(masp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Lấy sản phẩm theo danh mục có phân trang — sắp xếp theo rating giảm dần
    @GetMapping("/category/{madm}/page")
    public ResponseEntity<Page<Product>> getSanPhamsByDanhMucPaginated(
            @PathVariable Integer madm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sanphamService.getSanPhamsByDanhMucPaginated(madm, page, size));
    }
}
