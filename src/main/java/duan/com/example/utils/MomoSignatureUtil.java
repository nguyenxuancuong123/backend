package duan.com.example.utils;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MomoSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private MomoSignatureUtil() {
    }

    public static String buildRawSignature(String accessKey, String amount, String extraData,
                                           String ipnUrl, String orderId, String orderInfo,
                                           String partnerCode, String redirectUrl,
                                           String requestId, String requestType) {
        return "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
    }

    public static String sign(String rawSignature, String secretKey) {
        try {
            Mac hmacSha256 = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            hmacSha256.init(secretKeySpec);
            byte[] hashBytes = hmacSha256.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Không thể tạo chữ ký MoMo: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}