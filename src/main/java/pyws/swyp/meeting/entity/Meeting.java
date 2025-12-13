package pyws.swyp.meeting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pyws.swyp.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.CREATED;

    @Column
    private LocalDateTime dateVoteDeadline;

    @Column
    private LocalDateTime courseVoteDeadline;

    @Builder
    public Meeting(
            String title,
            LocalDateTime date,
            LocalDateTime dateVoteDeadline,
            LocalDateTime courseVoteDeadline
    ) {
        this.title = title;
        this.date = date;
        this.dateVoteDeadline = dateVoteDeadline;
        this.courseVoteDeadline = courseVoteDeadline;
    }

    public void updateStatus(String status) {
        this.status = Status.valueOf(status);
    }
}
