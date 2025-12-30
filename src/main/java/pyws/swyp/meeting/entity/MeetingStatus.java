package pyws.swyp.meeting.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MeetingStatus {

    CREATED(1),
    DATE_VOTING(2),
    DATE_VOTED(3),
    TIME_VOTING(4),
    TIME_VOTED(5),
    PLACE_VOTING(6),
    PLACE_VOTED(7),
    FIXED(8),
    DONE(9);
    private final int level;

    public boolean isNotVotable() {
        return this == FIXED || this == DONE;
    }

    public boolean isDateVotable() {
        return this == CREATED || this == DATE_VOTING;
    }

    public boolean isTimeVotable() {
        return this == DATE_VOTED || this == TIME_VOTING;
    }

    public boolean isDateVoteVisible() {
        return this.level >= DATE_VOTING.level;
    }

    public boolean isTimeVoteVisible() {
        return this.level >= TIME_VOTING.level;
    }
}
