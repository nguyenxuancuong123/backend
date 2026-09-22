package duan.com.example.controller;

import duan.com.example.entity.Category;
import duan.com.example.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<Category> getAllDanhMucs() {
        return categoryService.getAllDanhMucs();
    }

    @GetMapping("/{madm}")
    public ResponseEntity<Category> getDanhMucById(@PathVariable Integer madm) {
        return categoryService.getDanhMucById(madm)
                .map(ResponseEntity::ok) // nhận đối tượng dc tìm thấy
                .orElse(ResponseEntity.notFound().build()); // ko tìm thấy trả về lỗi
    }

}
