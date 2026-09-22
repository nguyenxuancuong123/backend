package duan.com.example.repository;

import duan.com.example.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    // Kiểm tra token có trong blacklist không
    boolean existsByToken(String token);
}
