package pyws.swyp.auth.service;

import static pyws.swyp.global.error.ErrorCode.UNAUTHORIZED;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pyws.swyp.auth.dto.JwtResponse;
import pyws.swyp.auth.repository.RefreshTokenRepository;
import pyws.swyp.global.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtResponse issueTokens(Long memberId) {
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);

        refreshTokenRepository.save(memberId, refreshToken);

        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse reissue(String refreshToken) {
        // Refresh Token 유효성 검증
        Boolean isValid = jwtProvider.validateToken(refreshToken, false);
        if (!isValid) {
            throw UNAUTHORIZED.toException();
        }

        Long memberId = jwtProvider.getMemberId(refreshToken);

        // Redis에 저장된 Refresh Token과 일치하는지 확인
        boolean matches = refreshTokenRepository.matches(memberId, refreshToken);
        if (!matches) {  // 폐기된 Refresh Token일 가능성 -> 로그아웃 처리
            refreshTokenRepository.delete(memberId);
            throw UNAUTHORIZED.toException();
        }

        // 재발급하여 반환
        return issueTokens(memberId);
    }

    public void logout(Long memberId) {
        refreshTokenRepository.delete(memberId);
    }
}
