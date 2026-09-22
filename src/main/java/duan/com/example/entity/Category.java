package duan.com.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "danhmuc")
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "madm", nullable = false)
    private Integer maDM;

    @Column(name = "tendm", nullable = false, length = 100)
    private String tenDM;

    @Column(name = "mota", length = 200)
    private String moTa;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Product> products;

}