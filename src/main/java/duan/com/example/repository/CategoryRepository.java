package duan.com.example.repository;

import duan.com.example.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // tìm kiếm theo tên danh mục
    List<Category> findByTenDM(String tenDM);
}
