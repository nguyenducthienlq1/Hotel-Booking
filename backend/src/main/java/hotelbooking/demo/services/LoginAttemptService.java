package hotelbooking.demo.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;

    // CẤU HÌNH: Sai 5 lần thì khóa 30 phút
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 30; // Phút

    public LoginAttemptService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Tạo key theo email để không bị trùng (Ví dụ: LOGIN_FAIL:thien@gmail.com)
    private String getKey(String email) {
        return "LOGIN_FAIL:" + email;
    }

    /**
     * 1. Xử lý khi đăng nhập thất bại
     */
    public void loginFailed(String email) {
        String key = getKey(email);

        // Tăng biến đếm lên 1. (Nếu chưa có thì Redis tự tạo và set = 1)
        Long attempts = redisTemplate.opsForValue().increment(key);

        // Nếu đây là lần sai đầu tiên -> Set thời gian hết hạn cho key này
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, LOCK_TIME_DURATION, TimeUnit.MINUTES);
        }
    }

    /**
     * 2. Xử lý khi đăng nhập thành công
     */
    public void loginSucceeded(String email) {
        // Xóa key đi để reset bộ đếm về 0
        redisTemplate.delete(getKey(email));
    }

    /**
     * 3. Kiểm tra xem user có đang bị khóa không
     */
    public boolean isBlocked(String email) {
        String key = getKey(email);
        Object value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            // Vì Redis lưu số nguyên, ta ép kiểu về Integer
            int attempts = (Integer) value;
            return attempts >= MAX_LOGIN_ATTEMPTS;
        }
        return false;
    }

    /**
     * 4. (Bonus) Lấy thời gian còn lại trước khi được mở khóa (giây)
     * Để hiển thị thông báo: "Vui lòng thử lại sau X phút"
     */
    public long getTimeRemaining(String email) {
        String key = getKey(email);
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (expire != null && expire > 0) ? expire : 0;
    }
}