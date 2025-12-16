package pyws.swyp.global.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(BAD_REQUEST, "COM0001", "잘못된 요청입니다."),

    // Redis
    REDIS_SERIALIZATION_ERROR(INTERNAL_SERVER_ERROR, "RED0001", "Redis 데이터 직렬화에 실패했습니다."),
    REDIS_DESERIALIZATION_ERROR(INTERNAL_SERVER_ERROR, "RED0002", "Redis 데이터 역직렬화에 실패했습니다."),

    // Security
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH0001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH0002", "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(NOT_FOUND, "MEM0001", "존재하지 않는 회원입니다."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(CONFLICT, "SOC0001", "이미 가입된 소셜 계정입니다."),

    // Meeting
    MEETING_NOT_FOUND(NOT_FOUND, "MEET0001", "존재하지 않는 모임입니다."),
    MEETING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEET0002", "해당 모임에 대한 접근 권한이 없습니다."),
    MEETING_QUIT_DENIED(HttpStatus.FORBIDDEN, "MEET003", "모임 생성자는 탈퇴할 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    public CustomException toException() {
        return new CustomException(this);
    }
}
