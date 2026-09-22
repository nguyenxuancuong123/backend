package duan.com.example.service;

import duan.com.example.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {

    // lấy danh sách danh mục
    List<Category> getAllDanhMucs();

    // lấy id danh mục
    Optional<Category> getDanhMucById(Integer madm);

    // thêm danh mục mới
    Category saveDanhMuc(Category category);

    // xóa danh mục
    void deleteDanhMuc(Integer madm);
}
