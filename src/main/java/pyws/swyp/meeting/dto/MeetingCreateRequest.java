package pyws.swyp.meeting.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pyws.swyp.meeting.entity.MeetingType;

public record MeetingCreateRequest(
        @NotBlank(message = "모임명을 입력해 주세요.")
        String title,
        @NotNull(message = "모임 유형을 선택해 주세요")
        MeetingType type
) {
}
