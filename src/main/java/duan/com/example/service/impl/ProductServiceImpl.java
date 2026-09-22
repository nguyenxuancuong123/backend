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
    public List<Product> getAllSanPhams() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getSanPhamsPaginated(int page, int size) {
        // PageRequest nhận index bắt đầu từ 0, nếu frontend gửi page = 1 thì trừ đi 1
        int pageIndex = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageIndex, size);
        return productRepository.findAll(pageable);
    }

    @Override
    public Optional<Product> getSanPhamById(Integer masp) {
        return productRepository.findById(masp);
    }

    @Override
    public List<Product> getSanPhamsByDanhMuc(Integer madm) {
        return productRepository.findByDM_Id(madm);
    }

    @Override
    public Page<Product> getSanPhamsByDanhMucPaginated(Integer madm, int page, int size) {
        // PageRequest nhận index bắt đầu từ 0, nếu frontend gửi page = 1 thì trừ đi 1
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
}