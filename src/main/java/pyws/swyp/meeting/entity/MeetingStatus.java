package pyws.swyp.meeting.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MeetingStatus {
    IN_PROGRESS,    // 모임 진행 중 (투표 중 / 일정 확정 후 대기)
    DONE            // 모임 종료됨 (후기 가능)
}
