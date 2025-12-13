package pyws.swyp.auth.dto;

public record AuthResponse(
        boolean signupRequired,
        JwtResponse tokens
) {
}
