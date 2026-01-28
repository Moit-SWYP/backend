package pyws.swyp.meeting.service;

import static pyws.swyp.global.error.ErrorCode.DATE_VOTE_NOT_FOUND;
import static pyws.swyp.global.error.ErrorCode.MEETING_HOST_ONLY;
import static pyws.swyp.global.error.ErrorCode.MEETING_NOT_CONFIRMABLE;
import static pyws.swyp.global.error.ErrorCode.MEETING_NOT_FOUND;
import static pyws.swyp.global.error.ErrorCode.MEETING_PARTICIPANT_NOT_FOUND;
import static pyws.swyp.global.error.ErrorCode.TIME_VOTE_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.event.DateVoteConfirmedEvent;
import pyws.swyp.meeting.event.TimeVoteConfirmedEvent;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingConfirmService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final DateVoteRepository dateVoteRepository;
    private final TimeVoteRepository timeVoteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void confirmDateVote(Long memberId, Long meetingId) {
        Meeting meeting = getMeeting(meetingId);
        MeetingParticipant participant = getParticipant(memberId, meetingId);

        validateHost(participant);
        validateNotDone(meeting);

        List<LocalDate> topDates = dateVoteRepository.findTopDatesByMeetingId(meetingId,
                PageRequest.of(0, 1));
        if (topDates.isEmpty()) {
            throw DATE_VOTE_NOT_FOUND.toException();
        }

        LocalDate topDate = topDates.getFirst();

        meeting.confirmDate(topDate);
        eventPublisher.publishEvent(new DateVoteConfirmedEvent(meetingId));
    }

    public void confirmDate(Long memberId, Long meetingId, LocalDate date) {
        Meeting meeting = getMeeting(meetingId);
        MeetingParticipant participant = getParticipant(memberId, meetingId);

        validateHost(participant);
        validateNotDone(meeting);

        meeting.confirmDate(date);
        eventPublisher.publishEvent(new DateVoteConfirmedEvent(meetingId));
    }

    public void cancelConfirmDateVote(Long memberId, Long meetingId) {
        Meeting meeting = getMeeting(meetingId);
        MeetingParticipant participant = getParticipant(memberId, meetingId);

        validateHost(participant);
        validateDateCancelable(meeting);

        meeting.cancelConfirmedDate();
    }

    public void confirmTimeVote(Long memberId, Long meetingId) {
        Meeting meeting = getMeeting(meetingId);
        MeetingParticipant participant = getParticipant(memberId, meetingId);

        validateHost(participant);
        validateNotDone(meeting);

        List<LocalTime> topTimes = timeVoteRepository.findTopTimesByMeetingId(meetingId, PageRequest.of(0, 1));
        if (topTimes.isEmpty()) {
            throw TIME_VOTE_NOT_FOUND.toException();
        }

        meeting.confirmTime(topTimes.getFirst());
        eventPublisher.publishEvent(new TimeVoteConfirmedEvent(meetingId));
    }

    public void confirmTime(Long memberId, Long meetingId, LocalTime time) {
        Meeting meeting = getMeeting(meetingId);
        MeetingParticipant participant = getParticipant(memberId, meetingId);

        validateHost(participant);
        validateNotDone(meeting);
        validateTimeUnit(time);

        meeting.confirmTime(time);
        eventPublisher.publishEvent(new TimeVoteConfirmedEvent(meetingId));
    }

    public void cancelConfirmTimeVote(Long memberId, Long meetingId) {
        Meeting meeting = getMeeting(meetingId);
        MeetingParticipant participant = getParticipant(memberId, meetingId);

        validateHost(participant);
        validateTimeCancelable(meeting);

        meeting.cancelConfirmedTime();
    }

    private Meeting getMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(MEETING_NOT_FOUND::toException);
    }

    private MeetingParticipant getParticipant(Long memberId, Long meetingId) {
        return meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(MEETING_PARTICIPANT_NOT_FOUND::toException);
    }

    private void validateHost(MeetingParticipant participant) {
        if (!participant.isHost()) {
            throw MEETING_HOST_ONLY.toException();
        }
    }

    private void validateNotDone(Meeting meeting) {
        if (meeting.getStatus() == MeetingStatus.DONE) {
            throw MEETING_NOT_CONFIRMABLE.toException();
        }
    }

    private void validateDateCancelable(Meeting meeting) {
        validateNotDone(meeting);

        if (!meeting.isDateConfirmed()) {
            throw ErrorCode.MEETING_DATE_NOT_CONFIRMED.toException();
        }
    }

    private void validateTimeCancelable(Meeting meeting) {
        if (meeting.getStatus() == MeetingStatus.DONE) {
            throw ErrorCode.MEETING_NOT_TIME_CANCELABLE.toException();
        }

        if (!meeting.isTimeConfirmed()) {
            throw ErrorCode.MEETING_TIME_NOT_CONFIRMED.toException();
        }
    }

    private void validateTimeUnit(LocalTime time) {
        int minute = time.getMinute();
        if (minute != 0 && minute != 30) {
            throw ErrorCode.TIME_NOT_IN_30_MIN_UNIT.toException();
        }

        if (time.getSecond() != 0 || time.getNano() != 0) {
            throw ErrorCode.INVALID_TIME_VOTE_REQUEST.toException();
        }
    }
}
