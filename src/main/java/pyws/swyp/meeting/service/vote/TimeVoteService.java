package pyws.swyp.meeting.service.vote;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.vote.time.TimeVoteRequest;
import pyws.swyp.meeting.dto.vote.time.TopVotedTimeResponse;
import pyws.swyp.meeting.dto.vote.time.VotedTimeResponse;
import pyws.swyp.meeting.dto.vote.time.VotedTimesResponse;
import pyws.swyp.meeting.dto.vote.VoterResponse;
import pyws.swyp.meeting.dto.vote.VotersResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.vote.TimeVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeVoteService {

    private final TimeVoteRepository timeVoteRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    /**
     * 모임원이 시간 투표를 진행하며, 기존 투표가 존재할 경우 덮어쓴다.
     */
    @Transactional
    public void voteTimes(Long memberId, Long meetingId, TimeVoteRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);

        if (!meeting.getStatus().isTimeVotable()) {
            throw ErrorCode.MEETING_NOT_VOTABLE.toException();
        }

        meeting.updateStatus(MeetingStatus.TIME_VOTING);

        MeetingParticipant participant = meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND::toException);

        // 내 기존 투표 삭제
        timeVoteRepository.deleteAllByMeetingParticipantId(participant.getId());

        // 새로 투표한 시간 목록
        List<LocalTime> times = normalizeAndValidateTimes(request.times());

        // 새 투표 저장
        List<TimeVote> votes = times.stream()
                .map(t -> TimeVote.builder()
                        .meeting(meeting)
                        .meetingParticipant(participant)
                        .time(t)
                        .build())
                .toList();

        timeVoteRepository.saveAll(votes);
    }

    /**
     * 특정 모임에서 가장 많은 투표를 받은 시간 목록을 조회한다.<br>
     * 여러 시간이 동점일 경우 시간 오름차순으로 정렬하여 반환한다.
     */
    @Transactional(readOnly = true)
    public TopVotedTimeResponse getTopVotedTimes(Long memberId, Long meetingId, int limit) {
        validateMemberParticipation(memberId, meetingId);

        // 투표 수 기준 Top N, 동점이면 오름차순
        List<LocalTime> times = timeVoteRepository.findTopTimesByMeetingId(meetingId,
                PageRequest.of(0, limit));

        if (times.isEmpty()) {
            throw ErrorCode.TIME_VOTE_NOT_FOUND.toException();
        }

        return new TopVotedTimeResponse(times);
    }

    /**
     * 모임에서 투표된 시간들과 각 투표수를 조회한다.
     */
    @Transactional(readOnly = true)
    public VotedTimesResponse getVotedTimesWithCounts(Long memberId, Long meetingId) {
        validateMemberParticipation(memberId, meetingId);

        List<VotedTimeResponse> results =
                timeVoteRepository.findVotedTimesWithCounts(meetingId);

        return new VotedTimesResponse(results);
    }

    /**
     * 특정 시간에 투표한 모임원 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public VotersResponse getVotersByTime(Long memberId, Long meetingId, LocalTime time) {
        validateMemberParticipation(memberId, meetingId);

        List<VoterResponse> voters = timeVoteRepository.findVotersByMeetingIdAndTime(meetingId, validateTimeUnit(time));

        return new VotersResponse(voters);
    }

    private List<LocalTime> normalizeAndValidateTimes(List<LocalTime> times) {
        if (times == null || times.isEmpty()) {
            throw ErrorCode.INVALID_TIME_VOTE_REQUEST.toException();
        }

        return times.stream()
                .map(this::validateTimeUnit)
                .distinct()
                .sorted()
                .toList();
    }

    private LocalTime validateTimeUnit(LocalTime time) {
        int minute = time.getMinute();
        if (minute != 0 && minute != 30) {
            throw ErrorCode.TIME_NOT_IN_30_MIN_UNIT.toException();
        }

        if (time.getSecond() != 0 || time.getNano() != 0) {
            throw ErrorCode.INVALID_TIME_VOTE_REQUEST.toException();
        }
        return time;
    }

    private void validateMemberParticipation(Long memberId, Long meetingId) {
        if (!meetingRepository.existsById(meetingId)) {
            throw ErrorCode.MEETING_NOT_FOUND.toException();
        }

        if (!meetingParticipantRepository.existsByMemberIdAndMeetingId(memberId, meetingId)) {
            throw ErrorCode.MEETING_PARTICIPANT_NOT_FOUND.toException();
        }
    }
}
