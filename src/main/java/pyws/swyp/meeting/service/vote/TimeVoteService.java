package pyws.swyp.meeting.service.vote;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.vote.TimeVoteRequest;
import pyws.swyp.meeting.dto.vote.TimeVoterResponse;
import pyws.swyp.meeting.dto.vote.TimeVotersResponse;
import pyws.swyp.meeting.dto.vote.TopVotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VotedTimesResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.vote.TimeOption;
import pyws.swyp.meeting.entity.vote.TimeVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.TimeOptionRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.entity.Member;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeVoteService {

    private final TimeOptionRepository timeOptionRepository;
    private final TimeVoteRepository timeVoteRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    /**
     * 모임원이 시간 투표를 진행하며, 기존 투표가 존재할 경우 덮어쓴다.<br>
     * 동시에 동일한 시간 후보가 생성될 수 있어 TimeOption은 조회/생성 로직으로 처리한다.
     */
    @Transactional
    public void voteTimes(Long memberId, Long meetingId, TimeVoteRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);

        // 이미 확정됐거나 완료된 모임일 경우
        if (meeting.getStatus().isNotVotable()) {
            throw ErrorCode.MEETING_NOT_VOTABLE.toException();
        }

        // 날짜 투표 확정 후 투표 가능
        // if (meeting.getStatus() != MeetingStatus.DATE_VOTING) {
        //     throw ErrorCode.MEETING_NOT_VOTABLE.toException();
        // }

        MeetingParticipant participant = meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND::toException);

        // 새로 투표한 시간 목록
        List<LocalTime> times = normalizeAndValidateTimes(request.times());

        // 내 기존 투표 내역
        List<TimeVote> prevTimeVotes = timeVoteRepository.findAllByMeetingParticipantId(participant.getId());

        // 기존 투표가 참조하던 TimeOption id 목록
        List<Long> prevOptionIds = prevTimeVotes.stream()
                .map(v -> v.getTimeOption().getId())
                .distinct()
                .toList();

        // 내 기존 투표 삭제
        if (!prevTimeVotes.isEmpty()) {
            timeVoteRepository.deleteAllInBatch(prevTimeVotes);
        }

        Map<LocalTime, TimeOption> optionMap = getOrCreateOptions(meeting, times);

        // 새 투표 저장
        List<TimeVote> votes = times.stream()
                .map(t -> TimeVote.builder()
                        .meetingParticipant(participant)
                        .timeOption(optionMap.get(t))
                        .build())
                .toList();

        timeVoteRepository.saveAll(votes);

        // 고아 투표 후보 제거
        if (!prevOptionIds.isEmpty()) {
            cleanupOrphanOptions(prevOptionIds);
        }
    }

    /**
     * 최다 득표 시간 1개를 조회한다.<br>
     * 동점일 경우 시간 오름차순으로 정렬하여 반환한다.
     */
    @Transactional(readOnly = true)
    public TopVotedTimeResponse getTopVotedTime(Long memberId, Long meetingId) {
        validateMemberParticipation(memberId, meetingId);

        LocalTime time = timeOptionRepository.findTopRankedTimeByMeetingId(meetingId)
                .orElseThrow(ErrorCode.TIME_VOTE_NOT_FOUND::toException);

        return new TopVotedTimeResponse(time);
    }

    /**
     * 모임에서 투표된 시간들과 각 투표수를 조회한다.
     */
    @Transactional(readOnly = true)
    public VotedTimesResponse getVotedTimesWithCounts(Long memberId, Long meetingId) {
        validateMemberParticipation(memberId, meetingId);

        List<VotedTimeResponse> results =
                timeOptionRepository.findVotedTimesWithCounts(meetingId);

        return new VotedTimesResponse(results);
    }

    /**
     * 특정 시간에 투표한 모임원 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public TimeVotersResponse getVotersByTime(Long memberId, Long meetingId, LocalTime time) {
        validateMemberParticipation(memberId, meetingId);

        List<TimeVote> votes = timeVoteRepository.findAllByMeetingIdAndTime(meetingId, validateTimeUnit(time));

        List<TimeVoterResponse> voters = votes.stream()
                .map(tv -> {
                    Member member = tv.getMeetingParticipant().getMember();
                    return new TimeVoterResponse(
                            member.getId(),
                            member.getNickname(),
                            member.getCharacterType()
                    );
                })
                .toList();

        return new TimeVotersResponse(voters);
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

    /**
     * 시간 목록에 대해 시간(LocalTime) -> 시간 후보(TimeOption)로 매핑하여 반환한다.<br>
     * 이미 존재하는 후보는 그대로 매핑하고, 새로운 시간은 새로 생성/저장하여 map에 합친다.
     */
    private Map<LocalTime, TimeOption> getOrCreateOptions(Meeting meeting, List<LocalTime> times) {
        // 기존에 존재하는 시간 후보
        List<TimeOption> existing =
                timeOptionRepository.findAllByMeetingIdAndTimeIn(meeting.getId(), times);

        // 시간 -> 투표 후보 매핑
        Map<LocalTime, TimeOption> map = existing.stream()
                .collect(Collectors.toMap(TimeOption::getTime, o -> o));

        // 추가될 시간 후보
        List<TimeOption> toCreate = times.stream()
                .filter(t -> !map.containsKey(t))
                .map(t -> TimeOption.builder()
                        .meeting(meeting)
                        .time(t)
                        .build())
                .toList();

        // 새로 추가된 투표 후보 없음
        if (toCreate.isEmpty()) {
            return map;
        }

        try {
            List<TimeOption> created = timeOptionRepository.saveAll(toCreate);
            created.forEach(o -> map.put(o.getTime(), o));
            return map;
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 시간 투표를 할 경우(유니크 제약 조건) DB에서 최신값 조회하여 반환
            List<TimeOption> after =
                    timeOptionRepository.findAllByMeetingIdAndTimeIn(meeting.getId(), times);
            return after.stream()
                    .collect(Collectors.toMap(TimeOption::getTime, o -> o));
        }
    }

    /**
     * 어떤 투표에서도 사용되지 않는 고아 TimeOption을 정리한다.<br>
     * 동시 투표로 인한 충돌 시에는 예외를 무시하고 정리를 건너뛴다.
     */
    private void cleanupOrphanOptions(List<Long> optionIds) {
        // 투표를 실제로 한 후보
        List<Long> stillUsed = timeVoteRepository.findExistingOptionIds(optionIds);
        Set<Long> stillUsedSet = new HashSet<>(stillUsed);

        // 제거할 고아값
        List<Long> toDelete = optionIds.stream()
                .filter(id -> !stillUsedSet.contains(id))
                .toList();

        if (toDelete.isEmpty()) {
            return;
        }

        try {
            timeOptionRepository.deleteAllByIdInBatch(toDelete);
        } catch (DataIntegrityViolationException e) {
            log.debug("Skip orphan TimeOption cleanup due to concurrent usage. optionIds={}", toDelete);
        }
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
