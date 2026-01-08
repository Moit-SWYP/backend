package pyws.swyp.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import pyws.swyp.notification.entity.Notification;
import pyws.swyp.notification.entity.NotificationStatus;
import pyws.swyp.notification.entity.NotificationType;

@Builder
public record NotificationResponse(
        Long id,
        NotificationType type,
        NotificationStatus status,
        String title,
        String body,
        String deepLink,
        Long meetingId,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .status(n.getStatus())
                .title(n.getTitle())
                .body(n.getBody())
                .deepLink(n.getDeepLink())
                .meetingId(n.getMeetingId())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
