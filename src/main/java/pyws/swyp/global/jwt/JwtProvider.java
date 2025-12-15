package pyws.swyp.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties props;
    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] decoded = Base64.getDecoder().decode(props.getSecretKey());
        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(decoded);
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, "access", props.getAccessTokenTtlMs());
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, "refresh", props.getRefreshTokenTtlMs());
    }

    private String createToken(Long memberId, String type, long ttlMs) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(ttlMs);

        return Jwts.builder()
                .issuer(props.getIssuer())
                .subject(String.valueOf(memberId))
                .claim("typ", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.getIssuer())
                .build()
                .parseSignedClaims(token);
    }

    public Boolean validateToken(String token, Boolean isAccessToken) {
        try {
            Object typeObj = parse(token).getPayload().get("typ");
            if (typeObj == null) {
                return false;
            }
            String type = typeObj.toString();

            if (isAccessToken && !type.equals("access")) {
                return false;
            }
            if (!isAccessToken && !type.equals("refresh")) {
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        return Long.parseLong(parse(token).getPayload().getSubject());
    }
}
