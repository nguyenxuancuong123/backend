package duan.com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "mausac")
@NoArgsConstructor
@AllArgsConstructor
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mamau", nullable = false)
    private Integer maMau;

    @Column(name = "tenmau", nullable = false, length = 50)
    private String tenMau;

    @Column(name = "mamauhex", nullable = false, length = 7)
    private String maMauHex;

}