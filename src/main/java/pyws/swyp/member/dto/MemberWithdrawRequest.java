package pyws.swyp.member.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pyws.swyp.member.entity.WithdrawalType;

public record MemberWithdrawRequest(
        @NotNull(message = "탈퇴 사유를 선택해 주세요.")
        WithdrawalType type,

        @Size(max = 500, message = "기타 사유는 최대 500자까지 입력할 수 있습니다.")
        String description
) {
    @AssertTrue(message = "기타 사유를 입력해 주세요.")
    public boolean hasDescription() {
        if (type != WithdrawalType.ETC) return true;
        return description != null && !description.isBlank();
    }
}
