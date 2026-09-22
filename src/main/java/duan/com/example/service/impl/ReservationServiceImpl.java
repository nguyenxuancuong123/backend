package duan.com.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String RESERVE_KEY = "reserve:product:";
    private static final long RESERVE_TTL_MINUTES = 10;

    /**
     * Giữ chỗ sản phẩm khi user bấm "Tiến hành thanh toán"
     * @return true nếu giữ chỗ thành công, false nếu đã bị người khác giữ
     */
    public boolean reserveProduct(Long masp, Long userId, int soLuong) {
        String key = RESERVE_KEY + masp + ":" + userId;
        String countKey = RESERVE_KEY + masp + ":count"; // tổng số lượng đang bị reserve

        // Dùng Lua script để đảm bảo atomic
        String luaScript = """
            local countKey = KEYS[1]
            local userKey = KEYS[2]
            local requestedQty = tonumber(ARGV[1])
            local stock = tonumber(ARGV[2])
            local ttl = tonumber(ARGV[3])
            local userId = ARGV[4]
            
            local currentReserved = tonumber(redis.call('GET', countKey) or '0')
            local available = stock - currentReserved
            
            if available < requestedQty then
                return 0  -- Không đủ hàng
            end
            
            redis.call('SET', userKey, requestedQty, 'EX', ttl)
            redis.call('INCRBY', countKey, requestedQty)
            redis.call('EXPIRE', countKey, ttl)
            return 1  -- Thành công
            """;

        // Lấy tồn kho hiện tại từ DB trước khi gọi
        // (method này được gọi từ Controller sau khi query DB)

        Boolean result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Boolean.class),
                List.of(RESERVE_KEY + masp + ":count", key),
                String.valueOf(soLuong),
                // stock được truyền từ ngoài vào
                "0", // placeholder, xem cách dùng đầy đủ
                String.valueOf(RESERVE_TTL_MINUTES * 60),
                String.valueOf(userId)
        );
        return Boolean.TRUE.equals(result);
    }

    /**
     * Giải phóng reservation khi user hủy hoặc thanh toán xong
     */
    public void releaseReservation(Long masp, Long userId) {
        String key = RESERVE_KEY + masp + ":" + userId;
        String countKey = RESERVE_KEY + masp + ":count";

        String qty = stringRedisTemplate.opsForValue().get(key);
        if (qty != null) {
            stringRedisTemplate.opsForValue().decrement(countKey, Long.parseLong(qty));
            stringRedisTemplate.delete(key);
        }
    }

    /**
     * Lấy số lượng đang bị reserve của 1 sản phẩm
     */
    public int getReservedCount(Long masp) {
        String val = stringRedisTemplate.opsForValue().get(RESERVE_KEY + masp + ":count");
        return val == null ? 0 : Integer.parseInt(val);
    }
}
