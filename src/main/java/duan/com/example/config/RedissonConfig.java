package duan.com.example.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:${spring.redis.host:localhost}}")
    private String redisHost;

    @Value("${spring.data.redis.port:${spring.redis.port:6379}}")
    private int redisPort;

    @Value("${spring.data.redis.password:${spring.redis.password:}}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        var singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(50);

        // Tự động cấu hình password nếu có khai báo trong application.properties
        if (redisPassword != null && !redisPassword.isBlank()) {
            singleServerConfig.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}