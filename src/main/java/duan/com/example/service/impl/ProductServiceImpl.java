package duan.com.example.service.impl;

import duan.com.example.entity.Product;
import duan.com.example.repository.ProductRepository;
import duan.com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;


    @Override
    public Page<Product> getSanPhamsPaginated(int page, int size) {
        // Sắp xếp theo rating giảm dần (PageRequest không cần Sort vì query đã có ORDER BY)
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size);
        return productRepository.findAllOrderByRatingDesc(pageable);
    }

    @Override
    public Optional<Product> getSanPhamById(Integer masp) {
        return productRepository.findById(masp);
    }


    @Override
    public Page<Product> getSanPhamsByDanhMucPaginated(Integer madm, int page, int size) {
        // Sắp xếp theo rating giảm dần (query đã có ORDER BY)
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size);
        return productRepository.findByDM_Id(madm, pageable);
    }

    @Override
    public Product saveSanPham(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteSanPham(Integer masp) {
        productRepository.deleteById(masp);
    }

    @Override
    public Page<Product> searchByKeyword(String keyword, int page, int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        // Dùng un-paged Pageable vì query đã có ORDER BY — chỉ cần offset/limit
        Pageable pageable = PageRequest.of(pageIndex, size);
        return productRepository.searchByKeyword(keyword.trim(), pageable);
    }
}