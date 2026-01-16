package pyws.swyp.notification.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pyws.swyp.meeting.event.ReviewReminderEvent;
import pyws.swyp.notification.service.MeetingNotificationService;

@Component
@RequiredArgsConstructor
public class ReviewNotificationListener {

    private final MeetingNotificationService meetingNotificationService;

    @EventListener
    public void onVoteStarted(ReviewReminderEvent event) {
        meetingNotificationService.remindRecord(event.meetingId());
    }
}
