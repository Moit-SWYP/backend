package pyws.swyp.meeting.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MeetingStatus {

    CREATED(1),
    DATE_VOTING(2),
    PLACE_VOTING(3),
    FIXED(4),
    DONE(5),
    ;

    private final int level;

    public boolean isNotVotable() {
        return this == FIXED || this == DONE;
    }
}
