package pyws.swyp.meeting.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Status {

    CREATED(1),
    VOTING(2),
    FIXED(3),
    DONE(4),
    ;

    private final int level;
}
