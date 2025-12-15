package pyws.swyp.global.component;

import static pyws.swyp.global.error.ErrorCode.REDIS_DESERIALIZATION_ERROR;
import static pyws.swyp.global.error.ErrorCode.REDIS_SERIALIZATION_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisComponent {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setString(String key, String value, long ttlMs) {
        redisTemplate.opsForValue().set(key, value, ttlMs, TimeUnit.MILLISECONDS);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public <T> T getJson(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw REDIS_DESERIALIZATION_ERROR.toException();
        }
    }

    public <T> void setJson(String key, T value, long ttlMs) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue()
                    .set(key, json, ttlMs, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            throw REDIS_SERIALIZATION_ERROR.toException();
        }
    }
}
