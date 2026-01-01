package pyws.swyp.notification.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pyws.swyp.meeting.event.DateVoteConfirmedEvent;
import pyws.swyp.meeting.event.TimeVoteConfirmedEvent;
import pyws.swyp.meeting.event.VoteStartedEvent;
import pyws.swyp.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
public class VoteNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoteStarted(VoteStartedEvent event) {
        notificationService.notifyVoteStarted(event.meetingId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDateVoteConfirmed(DateVoteConfirmedEvent event) {
        notificationService.notifyDateVoteConfirmed(event.meetingId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTimeVoteConfirmed(TimeVoteConfirmedEvent event) {
        notificationService.notifyTimeVoteConfirmed(event.meetingId());
    }
}
