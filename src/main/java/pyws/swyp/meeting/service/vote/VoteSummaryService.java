package pyws.swyp.meeting.service.vote;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.vote.VoteSummary;
import pyws.swyp.meeting.dto.vote.date.DateSummary;
import pyws.swyp.meeting.dto.vote.time.TimeSummary;
import pyws.swyp.meeting.dto.vote.time.VotedTimeResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;

@Service
@RequiredArgsConstructor
public class VoteSummaryService {

    private static final int TOP_N = 3;

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final DateVoteRepository dateVoteRepository;
    private final TimeVoteRepository timeVoteRepository;

    @Transactional(readOnly = true)
    public VoteSummary getVoteSummary(Long memberId, Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);

        MeetingParticipant participant = meetingParticipantRepository
                .findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND::toException);

        MeetingStatus status = meeting.getStatus();
        boolean isHost = participant.isHost();

        DateSummary dateSummary = status.isDateVoteVisible()
                ? getDateSummary(meetingId)
                : null;
        TimeSummary timeSummary = status.isTimeVoteVisible()
                ? getTimeSummary(meetingId)
                : null;

        return new VoteSummary(
                meeting.getStatus(),
                isHost,
                meeting.getDate(),
                meeting.getTime(),
                dateSummary,
                timeSummary
        );
    }

    private DateSummary getDateSummary(Long meetingId) {
        List<LocalDate> topDates = dateVoteRepository.findTopDatesByMeetingId(
                meetingId, PageRequest.of(0, TOP_N)
        );
        List<LocalDate> votedDates = dateVoteRepository.findVotedDatesByMeetingId(meetingId);

        return new DateSummary(topDates, votedDates);
    }

    private TimeSummary getTimeSummary(Long meetingId) {
        List<LocalTime> topTimes = timeVoteRepository.findTopTimesByMeetingId(
                meetingId, PageRequest.of(0, TOP_N)
        );
        List<VotedTimeResponse> votedTimes = timeVoteRepository.findVotedTimesWithCounts(meetingId);

        return new TimeSummary(topTimes, votedTimes);
    }
}
