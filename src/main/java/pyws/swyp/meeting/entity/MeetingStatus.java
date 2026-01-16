package pyws.swyp.meeting.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MeetingStatus {

    VOTING,   // 모임 생성됨 (투표 가능)
    FIXED,     // 일정 확정됨 (투표 종료)
    DONE;      // 모임 종료됨 (후기 가능)

    public boolean isVotable() {
        return this == VOTING;
    }

    public boolean isEnded() {
        return this == DONE;
    }
}
