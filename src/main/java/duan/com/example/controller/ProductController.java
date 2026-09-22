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

    // lấy tất cả sản phẩm
    @GetMapping
    public List<Product> getAllSanPhams() {
        return sanphamService.getAllSanPhams();
    }

    // lấy danh sách sản phẩm phân trang
    @GetMapping("/page")
    public ResponseEntity<Page<Product>> getSanPhamsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> sanPhamPage = sanphamService.getSanPhamsPaginated(page, size);
        return ResponseEntity.ok(sanPhamPage);
    }

    // lấy sản phẩm theo (id)
    @GetMapping("/{masp}")
    public ResponseEntity<Product> getSanPhamById(@PathVariable Integer masp) {
        return sanphamService.getSanPhamById(masp)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // lấy sản phẩm theo danh mục
    @GetMapping("/category/{madm}")
    public List<Product> getSanPhamsByDanhMuc(@PathVariable Integer madm) {
        return sanphamService.getSanPhamsByDanhMuc(madm);
    }

    // lấy sản phẩm theo danh mục có phân trang
    @GetMapping("/category/{madm}/page")
    public ResponseEntity<Page<Product>> getSanPhamsByDanhMucPaginated(
            @PathVariable Integer madm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> sanPhamPage = sanphamService.getSanPhamsByDanhMucPaginated(madm, page, size);
        return ResponseEntity.ok(sanPhamPage);
    }
}
