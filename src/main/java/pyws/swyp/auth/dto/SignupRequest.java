package pyws.swyp.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pyws.swyp.member.entity.Gender;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    @NotNull(message = "로그인 정보는 필수입니다.")
    @Valid
    private LoginRequest login;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 100, message = "닉네임은 100자 이내로 입력해 주세요.")
    private String nickname;

    @NotNull(message = "생년월일을 입력해 주세요.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthday;

    @NotNull(message = "성별을 선택해 주세요.")
    private Gender gender;
}
