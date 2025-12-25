package pyws.swyp.meeting.entity.vote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.meeting.entity.MeetingParticipant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_participant_time_option",
                        columnNames = {"meeting_participant_id", "time_option_id"}
                )
        }
)
public class TimeVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_participant_id", nullable = false)
    private MeetingParticipant meetingParticipant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_option_id", nullable = false)
    private TimeOption timeOption;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TimeVote(MeetingParticipant meetingParticipant, TimeOption timeOption) {
        this.meetingParticipant = meetingParticipant;
        this.timeOption = timeOption;
    }
}
