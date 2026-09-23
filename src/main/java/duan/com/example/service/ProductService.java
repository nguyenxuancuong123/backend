package duan.com.example.service;

import duan.com.example.entity.Product;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    // lấy danh sách sản phẩm có phân trang và sắp xếp
    Page<Product> getSanPhamsPaginated(int page, int size);

    // tìm kiếm id sản phẩm
    Optional<Product> getSanPhamById(Integer masp);

    // tìm sản phẩm trong danh mục có phân trang
    Page<Product> getSanPhamsByDanhMucPaginated(Integer madm, int page, int size);

    // lưu sản phẩm
    Product saveSanPham(Product product);

    // xóa sản phẩm
    void deleteSanPham(Integer masp);

    // tìm kiếm sản phẩm theo từ khóa (có phân trang, sắp xếp theo rating)
    Page<Product> searchByKeyword(String keyword, int page, int size);
}
