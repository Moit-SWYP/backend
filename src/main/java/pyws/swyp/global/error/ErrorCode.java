package pyws.swyp.global.error;

import static org.springframework.http.HttpStatus.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(BAD_REQUEST,"COM0001", "잘못된 요청입니다."),
    MEMBER_NOT_FOUND(NOT_FOUND, "MEM0001", "존재하지 않는 회원입니다."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(CONFLICT, "SOC0001", "이미 가입된 소셜 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
