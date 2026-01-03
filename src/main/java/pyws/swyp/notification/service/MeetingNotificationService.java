package pyws.swyp.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.notification.dto.NotificationCommand;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingNotificationService {

    private final MeetingParticipantRepository meetingParticipantRepository;
    private final NotificationSender notificationSender;

    /**
     * 모임 생성 시 모임원 전체에게 투표 시작 알림을 발송한다.
     */
    public void notifyVoteStarted(Long meetingId) {
        notifyMeetingMembers(meetingId, NotificationCommand.voteStarted(meetingId));
    }

    /**
     * 날짜 투표 결과 확정 시 모임원 전체에게 알림을 발송한다.
     */
    public void notifyDateVoteConfirmed(Long meetingId) {
        notifyMeetingMembers(meetingId, NotificationCommand.dateVoteConfirmed(meetingId));
    }

    /**
     * 시간 투표 결과 확정 시 모임원 전체에게 알림을 발송한다.
     */
    public void notifyTimeVoteConfirmed(Long meetingId) {
        notifyMeetingMembers(meetingId, NotificationCommand.timeVoteConfirmed(meetingId));
    }

    /**
     * 날짜 투표 미참여자에게 알림을 발송한다.
     */
    public void remindDateVote(Long meetingId) {
        List<Long> memberIds = meetingParticipantRepository.findMemberIdsNotVotedDate(meetingId);
        notifyMemberIds(meetingId, memberIds, NotificationCommand.dateVoteReminder(meetingId));
    }

    /**
     * 시간 투표 미참여자에게 알림을 발송한다.
     */
    public void remindTimeVote(Long meetingId) {
        List<Long> memberIds = meetingParticipantRepository.findMemberIdsNotVotedTime(meetingId);
        notifyMemberIds(meetingId, memberIds, NotificationCommand.timeVoteReminder(meetingId));
    }

    /**
     * 모임 후기 미기록자에게 알림을 발송한다.
     */
    public void remindReview(Long meetingId) {
        List<Long> memberIds = meetingParticipantRepository.findMemberIdsNotWrittenReview(meetingId);
        notifyMemberIds(meetingId, memberIds, NotificationCommand.reviewReminder(meetingId));
    }

    /**
     * 모임에 속한 모든 참여자를 조회하여 알림 발송을 트리거한다.<br>
     * 알림 대상이 없을 경우 로그만 남기고 종료한다.
     */
    private void notifyMeetingMembers(Long meetingId, NotificationCommand cmd) {
        List<Long> memberIds = meetingParticipantRepository.findMemberIdsByMeetingId(meetingId);
        if (memberIds.isEmpty()) {
            log.info("No meeting members to notify. meetingId={}", meetingId);
            return;
        }
        notificationSender.createAndSendMulticast(memberIds, cmd);
    }

    /**
     * 특정 모임원들에게만 알림 발송을 트리거한다.
     */
    private void notifyMemberIds(Long meetingId, List<Long> memberIds, NotificationCommand cmd) {
        if (memberIds.isEmpty()) {
            log.info("No members to notify. meetingId={}, type={}", meetingId, cmd.type());
            return;
        }
        notificationSender.createAndSendMulticast(memberIds, cmd);
    }
}
