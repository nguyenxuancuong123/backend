package duan.com.example.repository;

import duan.com.example.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotifiRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findTop20ByUsers_EmailOrderByThoiGianDesc(String email);

    @Modifying
    @Transactional
    @Query("update Notification t set t.daDoc = true where t.users.email = :email and t.daDoc = false")
    int tickRead(@Param("email") String email);
}
