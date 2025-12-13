package pyws.swyp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pyws.swyp.member.entity.SocialProvider;

public record LoginRequest(
        @NotNull(message = "소셜 로그인 제공자는 필수입니다.")
        SocialProvider socialProvider,

        @NotBlank(message = "소셜 ID는 필수입니다.")
        String socialId,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email
) {
}
