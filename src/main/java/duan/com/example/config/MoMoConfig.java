package duan.com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "momo")
@Data
public class MoMoConfig {
    private String accessKey;
    private String secretKey;
    private String partnerCode;
    private String partnerName = "Test";
    private String storeId = "MomoTestStore";
    private String redirectUrl;
    private String ipnUrl;
    private String requestType = "payWithMethod";
    private String lang = "vi";
    private boolean autoCapture = true;
    private String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
}