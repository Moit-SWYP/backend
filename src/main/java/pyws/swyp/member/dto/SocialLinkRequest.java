package pyws.swyp.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pyws.swyp.member.entity.SocialProvider;

public record SocialLinkRequest(
        @NotNull(message = "소셜 로그인 제공자는 필수입니다.")
        SocialProvider socialProvider,

        @NotBlank(message = "소셜 ID는 필수입니다.")
        String socialId
) {
}
