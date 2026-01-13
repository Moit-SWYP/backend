package pyws.swyp.auth.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record OAuthProperties(
        Kakao kakao,
        Naver naver
) {
    public record Kakao(String appId, String adminKey) {}
    public record Naver(String clientId,String clientSecret) {}
}
