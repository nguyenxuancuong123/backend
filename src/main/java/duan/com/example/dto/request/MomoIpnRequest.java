package duan.com.example.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MomoIpnRequest {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;
    private Integer resultCode; // 0 = thanh toán thành công
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;
}
