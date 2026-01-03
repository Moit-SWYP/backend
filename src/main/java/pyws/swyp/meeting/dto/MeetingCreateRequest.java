package pyws.swyp.meeting.dto;


import jakarta.validation.constraints.NotBlank;
import pyws.swyp.meeting.entity.Meeting;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MeetingCreateRequest(
        @NotBlank(message = "모임명을 입력해 주세요.")
        String title,
        LocalDate date,
        LocalDateTime dateVoteDeadline,
        LocalDateTime courseVoteDeadline
) {
    public Meeting toMeetingEntity() {
        return Meeting.builder()
                .title(this.title)
                .date(this.date)
                .dateVoteDeadline(this.dateVoteDeadline)
                .courseVoteDeadline(this.courseVoteDeadline)
                .build();
    }
}
