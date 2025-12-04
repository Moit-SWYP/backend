package pyws.swyp.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestRequest {

    @NotNull(message = "name 은 필수입니다.")
    private String name;

    @Size(max = 2, message = "password 는 최대 2자까지 가능합니다.")
    private String password;
}
