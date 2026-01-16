package pyws.swyp.meeting.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record InviteFriendsRequest(
        @NotEmpty(message = "초대할 친구를 한 명 이상 선택해주세요.")
        List<
                @NotNull(message = "친구 ID는 null일 수 없습니다.")
                @Positive(message = "친구 ID는 0보다 커야 합니다.")
                Long
        > friendIds
) {
}
