package duan.com.example.service;

import duan.com.example.dto.response.MomoCreatePaymentResponse;
import duan.com.example.dto.request.MomoIpnRequest;

public interface MomoPaymentService {
    MomoCreatePaymentResponse createPayment(long amount, String orderInfo, Integer madh);

    void handleIpn(MomoIpnRequest ipnRequest);
}
