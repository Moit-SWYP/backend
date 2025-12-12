package pyws.swyp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private boolean signupRequired;
    private JwtResponse tokens;
}
