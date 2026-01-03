package pyws.swyp.member.dto.friend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GroupCreateRequest(
        @NotBlank(message = "그룹명을 올바르게 입력해주세요")
        String name,
        @Size(min = 2, message = "두 명 이상의 유저를 선택해주세요")
        List<Long> groupMemberIds
) {
}
