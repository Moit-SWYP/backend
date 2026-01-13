package pyws.swyp.meeting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UuidGenerator;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Filter(name = "activeFilter")
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
    @Column(nullable = false, length = 50)
    private MeetingStatus status = MeetingStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeetingType type;

    @Builder
    public Meeting(
            String title,
            MeetingType type
    ) {
        this.title = title;
        this.type = type;
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
    }

    public void confirmDate(LocalDate date) {
        this.date = date;
        updateStatus(MeetingStatus.DATE_VOTED);
    }

    public void cancelConfirmedDate() {
        this.date = null;
        updateStatus(MeetingStatus.DATE_VOTING);
    }

    public void confirmTime(LocalTime time) {
        this.time = time;
        updateStatus(MeetingStatus.TIME_VOTED);
    }

    public void cancelConfirmedTime() {
        this.time = null;
        updateStatus(MeetingStatus.TIME_VOTING);
    }
}
