package duan.com.example.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MomoCreatePaymentResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private Long responseTime;
    private String message;
    private Integer resultCode; // 0 = thành công
    private String payUrl;      // link để redirect user sang MoMo thanh toán
    private String deeplink;
    private String qrCodeUrl;
    private String deeplinkMiniApp;
}
