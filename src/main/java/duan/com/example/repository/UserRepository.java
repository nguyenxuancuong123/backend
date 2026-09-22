package duan.com.example.repository;

import duan.com.example.entity.Users;
import duan.com.example.entity.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByEmail(String email);

    Users findByVaiTro(Role vaiTro);
}
