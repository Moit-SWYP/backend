package pyws.swyp.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pyws.swyp.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_notification_member_created", columnList = "memberId, createdAt"),
        @Index(name = "idx_notification_member_status", columnList = "memberId, status")
})
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 200)
    private String body;

    @Column(length = 200)
    private String deepLink;

    private Long meetingId;

    @Column(length = 500)
    private String failReason;

    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    @Builder
    public Notification(Long memberId, NotificationType type, NotificationStatus status, String title, String body,
                        String deepLink, Long meetingId) {
        this.memberId = memberId;
        this.type = type;
        this.status = status;
        this.title = title;
        this.body = body;
        this.deepLink = deepLink;
        this.meetingId = meetingId;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.failReason = null;
    }

    public void markFailed(String reason) {
        this.status = NotificationStatus.FAILED;
        this.failReason = reason;
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}
