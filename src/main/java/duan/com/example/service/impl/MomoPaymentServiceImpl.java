package duan.com.example.service.impl;

import duan.com.example.dto.request.MomoCreatePaymentRequest;
import duan.com.example.dto.response.MomoCreatePaymentResponse;
import duan.com.example.dto.request.MomoIpnRequest;
import duan.com.example.dto.request.UpdateStatusRequest;
import duan.com.example.entity.Order;
import duan.com.example.repository.OrderRepository;
import duan.com.example.service.OrderService;
import duan.com.example.service.MomoPaymentService;
import duan.com.example.config.MoMoConfig;
import duan.com.example.utils.MomoSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomoPaymentServiceImpl implements MomoPaymentService {

    private final MoMoConfig momoConfig;
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // Nếu project đã có sẵn 1 bean RestTemplate dùng chung, nên @Autowired
    // bean đó thay vì new RestTemplate() ở đây để tận dụng connection pool.
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String EXTRA_DATA = "";
    private static final String ORDER_GROUP_ID = "";

    @Override
    public MomoCreatePaymentResponse createPayment(long amount, String orderInfo, Integer madh) {
        Order donHang = orderRepository.findById(madh)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + madh));
        String orderId = momoConfig.getPartnerCode() + System.currentTimeMillis();
        String requestId = orderId;
        String amountStr = String.valueOf(amount);

        // Gắn madh vào redirectUrl để trang FE sau khi thanh toán biết cần tra cứu đơn nào.
        // QUAN TRỌNG: chữ ký phải được ký trên ĐÚNG giá trị redirectUrl sẽ gửi lên MoMo
        // (không phải giá trị tĩnh trong config), nếu không MoMo sẽ báo lỗi "Chữ ký không hợp lệ".
        String redirectUrl = momoConfig.getRedirectUrl() + (momoConfig.getRedirectUrl().contains("?") ? "&" : "?")
                + "madh=" + madh;

        String rawSignature = MomoSignatureUtil.buildRawSignature(
                momoConfig.getAccessKey(), amountStr, EXTRA_DATA, momoConfig.getIpnUrl(),
                orderId, orderInfo, momoConfig.getPartnerCode(), redirectUrl,
                requestId, momoConfig.getRequestType());

        String signature = MomoSignatureUtil.sign(rawSignature, momoConfig.getSecretKey());

        MomoCreatePaymentRequest requestBody = MomoCreatePaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .partnerName(momoConfig.getPartnerName())
                .storeId(momoConfig.getStoreId())
                .requestId(requestId)
                .amount(amountStr)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(redirectUrl)
                .ipnUrl(momoConfig.getIpnUrl())
                .lang(momoConfig.getLang())
                .requestType(momoConfig.getRequestType())
                .autoCapture(momoConfig.isAutoCapture())
                .extraData(EXTRA_DATA)
                .orderGroupId(ORDER_GROUP_ID)
                .signature(signature)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoCreatePaymentRequest> httpEntity = new HttpEntity<>(requestBody, headers);

        MomoCreatePaymentResponse response;
        try {
            response = restTemplate.postForObject(momoConfig.getEndpoint(), httpEntity, MomoCreatePaymentResponse.class);
        } catch (Exception e) {
            // Gọi sang MoMo lỗi (mạng, timeout, MoMo trả 4xx/5xx...): đơn hàng #madh đã bị trừ
            // tồn kho ở bước tạo đơn trước đó (POST /api/v1/donhang) và sẽ KHÔNG BAO GIỜ nhận
            // được IPN vì giao dịch MoMo chưa từng được khởi tạo thành công.
            // => Phải tự hoàn kho ngay tại đây, không thể trông chờ vào handleIpn().
            log.error("Lỗi khi gọi API tạo thanh toán MoMo cho đơn hàng {}: {}", madh, e.getMessage(), e);
            huyVaHoanKhoDoTaoGiaoDichMomoThatBai(madh);
            throw new RuntimeException("Không thể khởi tạo thanh toán MoMo, đơn hàng đã được hủy và hoàn kho.", e);
        }

        // Chỉ lưu mapping orderId <-> madh nếu MoMo đã chấp nhận tạo giao dịch (resultCode == 0).
        // Đây là bước bắt buộc để handleIpn() sau này tra ngược được đơn hàng cần cập nhật,
        // vì IPN payload MoMo gửi về chỉ có "orderId" (do MoMo cấp), không có "madh".
        if (response != null && response.getResultCode() != null && response.getResultCode() == 0) {
            donHang.setMomoOrderId(orderId);
            orderRepository.save(donHang);
        } else {
            // MoMo trả về resultCode != 0 (vd sai chữ ký, sai partnerCode, amount không hợp lệ...):
            // giao dịch MoMo không được khởi tạo -> sẽ không có IPN nào gọi về sau này.
            // Cùng lý do như nhánh catch ở trên: phải hoàn kho ngay, không đợi IPN/scheduled job.
            log.warn("Tạo thanh toán MoMo thất bại cho đơn hàng {}: {}", madh,
                    response != null ? response.getMessage() : "no response");
            huyVaHoanKhoDoTaoGiaoDichMomoThatBai(madh);
        }

        return response;
    }

    // Hủy đơn + hoàn kho khi việc khởi tạo giao dịch MoMo thất bại (trước khi user kịp thanh toán).
    // Dùng chung logic với capNhatTrangThai("Hủy") đã có sẵn ở OrderServiceImpl (tự gọi hoanTonKho()).
    private void huyVaHoanKhoDoTaoGiaoDichMomoThatBai(Integer madh) {
        try {
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setTrangThai("Hủy");
            orderService.capNhatTrangThai(madh, request);
        } catch (Exception ex) {
            // Không để lỗi hoàn kho che mất lỗi gốc (lỗi gọi MoMo) đang được xử lý ở nơi gọi hàm này.
            log.error("Hoàn kho thất bại cho đơn hàng {} sau khi tạo giao dịch MoMo lỗi: {}", madh, ex.getMessage(), ex);
        }
    }

    @Override
    public void handleIpn(MomoIpnRequest ipn) {
        // 1. Verify chữ ký — KHÔNG được bỏ qua bước này, nếu không bất kỳ ai cũng có thể
        // giả mạo request POST vào /api/v1/momo/ipn để tự đánh dấu đơn hàng đã thanh toán.
        String rawSignature = "accessKey=" + momoConfig.getAccessKey()
                + "&amount=" + ipn.getAmount()
                + "&extraData=" + nullToEmpty(ipn.getExtraData())
                + "&message=" + ipn.getMessage()
                + "&orderId=" + ipn.getOrderId()
                + "&orderInfo=" + ipn.getOrderInfo()
                + "&orderType=" + ipn.getOrderType()
                + "&partnerCode=" + ipn.getPartnerCode()
                + "&payType=" + ipn.getPayType()
                + "&requestId=" + ipn.getRequestId()
                + "&responseTime=" + ipn.getResponseTime()
                + "&resultCode=" + ipn.getResultCode()
                + "&transId=" + ipn.getTransId();

        String expectedSignature = hmacSHA256(rawSignature, momoConfig.getSecretKey());

        if (!expectedSignature.equals(ipn.getSignature())) {
            log.warn("Chữ ký IPN MoMo không hợp lệ cho orderId={}", ipn.getOrderId());
            return; // bỏ qua, không cập nhật gì cả
        }

        // 2. Tra ngược ra đơn hàng nội bộ từ orderId MoMo đã cấp lúc tạo giao dịch
        Order donHang = orderRepository.findByMomoOrderId(ipn.getOrderId()).orElse(null);
        if (donHang == null) {
            log.warn("Không tìm thấy đơn hàng ứng với momoOrderId={}", ipn.getOrderId());
            return;
        }

        // 3. Idempotency: MoMo có thể gọi IPN nhiều lần cho cùng 1 giao dịch (retry khi
        // không nhận được response 2xx đủ nhanh). Nếu đơn hàng không còn ở "Chờ duyệt"
        // (đã được xử lý bởi lần gọi IPN trước) thì bỏ qua, tránh hoàn kho / trừ kho 2 lần.
        if (!"Chờ duyệt".equals(donHang.getTrangThai())) {
            log.info("Đơn hàng {} đã được xử lý trước đó (trạng thái hiện tại: {}), bỏ qua IPN lặp lại.",
                    donHang.getMaDonHang(), donHang.getTrangThai());
            return;
        }

        UpdateStatusRequest request = new UpdateStatusRequest();
        if (ipn.getResultCode() != null && ipn.getResultCode() == 0) {
            // Thanh toán thành công -> chuyển sang "Đang giao"
            request.setTrangThai("Đang giao");
        } else {
            // Thanh toán thất bại/bị hủy -> hủy đơn, capNhatTrangThai sẽ tự hoàn lại tồn kho
            request.setTrangThai("Hủy");
        }
        orderService.capNhatTrangThai(donHang.getMaDonHang(), request);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    // Chữ ký IPN của MoMo dùng thứ tự field khác với chữ ký lúc tạo giao dịch (create-payment),
    // nên viết riêng thay vì tái dùng MomoSignatureUtil.buildRawSignature() đã có.
    private String hmacSHA256(String data, String key) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi tạo chữ ký xác thực IPN MoMo", e);
        }
    }
}