package duan.com.example;

import duan.com.example.entity.Users;
import duan.com.example.entity.role.Role;
import duan.com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class DuanApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DuanApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Users adminAccount = userRepository.findByVaiTro(Role.Admin);

        if (null == adminAccount){
            Users user = new Users();

            user.setEmail("admin@gmail.com");
            user.setHoten("Nguyễn Xuân Cương");
            user.setVaiTro(Role.Admin);
            user.setMatKhau(new BCryptPasswordEncoder().encode("admin"));
            user.setDiaChi("Tân Phú");
            user.setSoDienThoai("0393849302");
            userRepository.save(user);
        }

    }
}


