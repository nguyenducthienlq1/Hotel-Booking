package hotelbooking.demo.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BaseRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public BaseRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 1. Lưu dữ liệu (Set)
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 2. Lưu dữ liệu có thời gian sống (Set with TTL) - Rất quan trọng cho Cache
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // 3. Lấy dữ liệu (Get)
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 4. Xóa dữ liệu (Delete)
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // 5. Kiểm tra key tồn tại
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 6. Set thời gian hết hạn cho key
    public void setTimeToLive(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }
}