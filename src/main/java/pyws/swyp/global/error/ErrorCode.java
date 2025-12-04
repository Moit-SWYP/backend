package pyws.swyp.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    FORBIDDEN("403", "접근 권한이 없습니다"),
    BAD_REQUEST("400", "잘못된 요청입니다."),
    EMAIL_ALREADY_EXISTS("409", "이미 가입된 이메일입니다."),
    MEMBER_NOT_FOUND("404", "존재하지 않는 회원입니다."),
    MEETING_NOT_FOUND("404", "존재하지 않는 모임입니다."),
    LOGIN_FAILED("401", "이메일 또는 비밀번호가 잘못 되었습니다."),
    UNAUTHORIZED("401", "로그인 해주세요.");

    private final String code;
    private final String message;
}
