package pyws.swyp.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
}
