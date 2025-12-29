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
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_participant_time",
                        columnNames = {"meeting_participant_id", "time"}
                )
        }
)
public class TimeVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_participant_id", nullable = false)
    private MeetingParticipant meetingParticipant;

    @Column(nullable = false)
    private LocalTime time;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TimeVote(Meeting meeting, MeetingParticipant meetingParticipant, LocalTime time) {
        this.meeting = meeting;
        this.meetingParticipant = meetingParticipant;
        this.time = time;
    }
}
