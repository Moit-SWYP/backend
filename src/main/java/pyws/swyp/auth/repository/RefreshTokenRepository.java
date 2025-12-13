package pyws.swyp.auth.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import pyws.swyp.global.jwt.JwtProperties;
import pyws.swyp.global.jwt.TokenHasher;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String PREFIX = "refresh:";

    private final JwtProperties props;
    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String refreshToken) {
        String hashed = TokenHasher.hash(refreshToken);
        redisTemplate.opsForValue().set(key(memberId), hashed, props.getRefreshTokenTtlMs(), TimeUnit.MILLISECONDS);
    }

    public String find(Long memberId) {
        return redisTemplate.opsForValue().get(key(memberId));
    }

    public void delete(Long memberId) {
        redisTemplate.delete(key(memberId));
    }

    public boolean matches(Long memberId, String refreshToken) {
        String hashed = TokenHasher.hash(refreshToken);
        String storedHashed = find(memberId);
        return hashed.equals(storedHashed);
    }

    private String key(Long memberId) {
        return PREFIX + memberId;
    }
}

