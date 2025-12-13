package pyws.swyp.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import pyws.swyp.member.entity.Gender;

public record SignupRequest(
        @NotNull(message = "로그인 정보는 필수입니다.")
        @Valid
        LoginRequest login,

        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(max = 100, message = "닉네임은 100자 이내로 입력해 주세요.")
        String nickname,

        @NotNull(message = "생년월일을 입력해 주세요.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate,

        @NotNull(message = "성별을 선택해 주세요.")
        Gender gender
) {
}
