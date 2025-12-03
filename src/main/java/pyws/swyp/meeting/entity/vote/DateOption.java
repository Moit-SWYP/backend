package pyws.swyp.meeting.entity.vote;

import jakarta.persistence.*;
import lombok.*;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.meeting.entity.Meeting;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DateOption extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private LocalDateTime candidateDate;
}
