package pyws.swyp.meeting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pyws.swyp.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_review_id", nullable = false)
    private MeetingReview meetingReview;

    @Column(nullable = false, length = 500)
    private String imageKey;

    @Column(nullable = false)
    private int sortOrder;

    @Builder
    public ReviewImage(MeetingReview meetingReview, String imageKey, int sortOrder) {
        this.meetingReview = meetingReview;
        this.imageKey = imageKey;
        this.sortOrder = sortOrder;
    }
}
