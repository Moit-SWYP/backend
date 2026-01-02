package pyws.swyp.notification.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop50ByMemberIdOrderByCreatedAtDescIdDesc(Long memberId);
}