package pyws.swyp.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record MeetingReviewCreate(
        @NotBlank(message = "모임 기록을 입력해 주세요.")
        String content,
        @Size(max = 5, message = "이미지는 최대 5개까지 등록할 수 있습니다.")
        List<String> imageKeys
) {
}
