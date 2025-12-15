package pyws.swyp.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pyws.swyp.global.component.RedisComponent;
import pyws.swyp.global.jwt.JwtProperties;
import pyws.swyp.global.jwt.TokenHasher;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String PREFIX = "refresh:";

    private final JwtProperties props;
    private final RedisComponent redisComponent;

    public void save(Long memberId, String refreshToken) {
        redisComponent.setString(
                key(memberId),
                TokenHasher.hash(refreshToken),
                props.getRefreshTokenTtlMs()
        );
    }

    public String find(Long memberId) {
        return redisComponent.getString(key(memberId));
    }

    public void delete(Long memberId) {
        redisComponent.delete(key(memberId));
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

