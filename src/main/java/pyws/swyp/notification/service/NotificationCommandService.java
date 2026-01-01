package pyws.swyp.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.notification.entity.Notification;
import pyws.swyp.notification.entity.NotificationStatus;
import pyws.swyp.notification.entity.NotificationType;
import pyws.swyp.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public List<Notification> createNotifications(
            List<Long> memberIds,
            NotificationType type,
            String title,
            String body,
            String deepLink,
            Long meetingId
    ) {
        List<Notification> notifications = memberIds.stream()
                .map(memberId -> Notification.builder()
                        .memberId(memberId)
                        .type(type)
                        .status(NotificationStatus.PENDING)
                        .title(title)
                        .body(body)
                        .deepLink(deepLink)
                        .meetingId(meetingId)
                        .build())
                .toList();

        return notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void updateStatuses(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
}

