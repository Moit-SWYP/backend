package pyws.swyp.auth.dto;

public record JwtResponse(
        String accessToken,
        String refreshToken
) {
}
