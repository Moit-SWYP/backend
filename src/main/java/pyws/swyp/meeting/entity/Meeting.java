package pyws.swyp.meeting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {

    @UuidGenerator
    @Column(nullable = false, unique = true, columnDefinition = "BINARY(16)")
    private UUID publicId;

    @Column(nullable = false)
    private String title;

    @Column
    private LocalDate date;

    @Column
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status = MeetingStatus.CREATED;

    @Column
    private LocalDateTime dateVoteDeadline;

    @Column
    private LocalDateTime courseVoteDeadline;

    @Builder
    public Meeting(
            String title,
            LocalDate date,
            LocalTime time,
            LocalDateTime dateVoteDeadline,
            LocalDateTime courseVoteDeadline
    ) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.dateVoteDeadline = dateVoteDeadline;
        this.courseVoteDeadline = courseVoteDeadline;
    }

    public void updateStatus(MeetingStatus status) {
        this.status = status;
    }

    public void update(MeetingUpdateRequest request) {
        if (request.title() != null) {
            if(request.title().isBlank()) {
                throw ErrorCode.MEETING_TITLE_EMPTY.toException();
            }
            this.title = request.title();
        }
        if (request.date() != null) {
            this.date = request.date();
        }
        if (request.dateVoteDeadline() != null) {
            this.dateVoteDeadline = request.dateVoteDeadline();
        }
        if (request.courseVoteDeadline() != null) {
            this.courseVoteDeadline = request.courseVoteDeadline();
        }
    }
}
