package pyws.swyp.meeting.entity.vote;

import jakarta.persistence.*;
import lombok.*;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.meeting.entity.MeetingParticipant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DateVote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_participant_id", nullable = false)
    private MeetingParticipant meetingParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_option_id", nullable = false)
    private DateOption dateOption;
}
