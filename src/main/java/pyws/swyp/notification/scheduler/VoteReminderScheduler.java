package pyws.swyp.notification.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.notification.service.MeetingNotificationService;

@Component
@RequiredArgsConstructor
public class VoteReminderScheduler {

    private final MeetingRepository meetingRepository;
    private final MeetingNotificationService meetingNotificationService;

    @Scheduled(cron = "0 0 10 * * *")
    public void remindVote() {

        // 날짜 투표 진행 중인 모임
        List<Long> dateVotingMeetingIds = meetingRepository.findIdsNeedingDateVoteReminder(MeetingStatus.VOTING);
        dateVotingMeetingIds.forEach(meetingNotificationService::remindDateVote);

        // 시간 투표 진행 중인 모임
        List<Long> timeVotingMeetingIds = meetingRepository.findIdsNeedingTimeVoteReminder(MeetingStatus.VOTING);
        timeVotingMeetingIds.forEach(meetingNotificationService::remindTimeVote);
    }
}
