package pyws.swyp.notification.scheduler;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pyws.swyp.meeting.event.ReviewReminderEvent;
import pyws.swyp.meeting.repository.MeetingRepository;

@Component
@RequiredArgsConstructor
public class MeetingReviewReminderScheduler {

    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher publisher;

    @Scheduled(cron = "0 0 12 * * *")
    public void remind() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 어제 완료된 모임 ID 목록
        List<Long> meetingIds = meetingRepository.findIdsForReviewReminder(yesterday);
        for (Long meetingId : meetingIds) {
            publisher.publishEvent(new ReviewReminderEvent(meetingId));
        }
    }
}
