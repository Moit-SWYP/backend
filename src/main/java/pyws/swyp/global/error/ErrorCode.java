package pyws.swyp.global.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(BAD_REQUEST, "COM0001", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM0002", "서버 오류가 발생했습니다."),

    // Redis
    REDIS_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RED0001", "Redis 데이터 직렬화에 실패했습니다."),
    REDIS_DESERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RED0002", "Redis 데이터 역직렬화에 실패했습니다."),

    // Security
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH0001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH0002", "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(NOT_FOUND, "MEM0001", "존재하지 않는 회원입니다."),
    INVALID_INVITE_MEMBER(BAD_REQUEST, "MEM0002", "존재하지 않는 멤버가 포함되어 있습니다"),

    SOCIAL_ACCOUNT_FOUND(NOT_FOUND, "SOC0001", "존재하지 않는 소셜 계정입니다."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(CONFLICT, "SOC0002", "이미 가입된 소셜 계정입니다."),

    // Meeting
    MEETING_NOT_FOUND(NOT_FOUND, "MEET0001", "존재하지 않는 모임입니다."),
    MEETING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MEET0002", "해당 모임에 대한 접근 권한이 없습니다."),
    MEETING_QUIT_DENIED(HttpStatus.FORBIDDEN, "MEET0003", "모임 생성자는 탈퇴할 수 없습니다."),
    MEETING_TITLE_EMPTY(BAD_REQUEST, "MEET0004", "모임 제목은 필수이며, 빈칸일 수 없습니다."),
    MEETING_NOT_VOTABLE(BAD_REQUEST, "MEET0005", "이미 확정됐거나 완료된 모임입니다."),
    MEETING_PARTICIPANT_NOT_FOUND(NOT_FOUND, "MEET0006", "존재하지 않는 모임원입니다."),
    MEETING_HOST_ONLY(HttpStatus.FORBIDDEN, "MEET0007", "모임장만 투표 확정 및 취소를 할 수 있습니다."),
    MEETING_NOT_CONFIRMABLE(HttpStatus.BAD_REQUEST, "MEET0008", "현재 모임 상태에서는 투표 확정 또는 취소를 할 수 없습니다."),
    HOST_CANNOT_WITHDRAW_WITH_UNCOMPLETED_MEETING(HttpStatus.BAD_REQUEST, "MEET0009", "완료되지 않은 모임의 모임장은 탈퇴할 수 없습니다."),
    MEETING_ALREADY_JOINED(CONFLICT, "MEET0010", "이미 참여중인 모임입니다."),

    // Vote
    DATE_VOTE_NOT_FOUND(NOT_FOUND, "VOTE0001", "아직 시간 투표가 존재하지 않습니다."),
    TIME_VOTE_NOT_FOUND(NOT_FOUND, "VOTE0002", "아직 시간 투표가 존재하지 않습니다."),
    INVALID_TIME_VOTE_REQUEST(BAD_REQUEST, "VOTE0003", "시간 투표 요청이 올바르지 않습니다."),
    TIME_NOT_IN_30_MIN_UNIT(BAD_REQUEST, "VOTE0004", "시간은 30분 단위로만 투표할 수 있습니다."),

    // Notification
    DEVICE_TOKEN_ALREADY_REGISTERED(HttpStatus.CONFLICT, "NOTI0001", "이미 등록된 디바이스 토큰입니다."),
    DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI0002", "디바이스 토큰을 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI0003", "알림을 찾을 수 없습니다."),
    NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "NOTI0004", "해당 알림에 대한 권한이 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    public CustomException toException() {
        return new CustomException(this);
    }
}
