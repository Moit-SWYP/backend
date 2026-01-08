package pyws.swyp.meeting.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pyws.swyp.meeting.service.MeetingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingCompletionScheduler {

    private final MeetingService meetingService;

    @Scheduled(cron = "0 0 0 * * *")
    public void completeExpiredMeetings() {
        log.info("[Scheduler] Expired meeting DONE processing started");
        meetingService.markMeetingsDone();
    }
}
