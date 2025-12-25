package pyws.swyp.meeting.dto.vote;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DateVoteRequest(
        @NotEmpty(message = "투표할 날짜를 최소 1개 이상 선택해야 합니다.")
        List<
                @NotNull(message = "날짜는 null일 수 없습니다.")
                        LocalDate
                > dates
) {
}
