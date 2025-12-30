package pyws.swyp.meeting.dto.vote.time;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

public record TimeVoteRequest(
        @NotEmpty(message = "투표할 시간을 최소 1개 이상 선택해야 합니다.")
        List<
                @NotNull(message = "시간은 null일 수 없습니다.")
                        LocalTime
                > times
) {
}
