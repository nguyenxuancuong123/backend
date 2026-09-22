package duan.com.example.service.impl;

import duan.com.example.entity.Category;
import duan.com.example.repository.CategoryRepository;
import duan.com.example.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository danhmucRepository;

    @Override
    public List<Category> getAllDanhMucs() {
        return danhmucRepository.findAll();
    }

    @Override
    public Optional<Category> getDanhMucById(Integer madm) {
        return danhmucRepository.findById(madm);
    }

    @Override
    public Category saveDanhMuc(Category category) {
        return danhmucRepository.save(category);
    }

    @Override
    public void deleteDanhMuc(Integer madm) {
        danhmucRepository.deleteById(madm);
    }
}