package pyws.swyp.meeting.service.vote;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.vote.DateVoteRequest;
import pyws.swyp.meeting.dto.vote.DateVoterResponse;
import pyws.swyp.meeting.dto.vote.DateVotersResponse;
import pyws.swyp.meeting.dto.vote.VotedDatesResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.vote.DateOption;
import pyws.swyp.meeting.entity.vote.DateVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateOptionRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.member.entity.Member;

@Slf4j
@Service
@RequiredArgsConstructor
public class DateVoteService {

    private final DateOptionRepository dateOptionRepository;
    private final DateVoteRepository dateVoteRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    /**
     * 모임원이 날짜 투표를 진행하며, 기존 투표가 존재할 경우 덮어쓴다.<br>
     * 동시에 동일한 날짜 후보가 생성될 수 있어 DateOption은 조회/생성 로직으로 처리한다.
     */
    @Transactional
    public void voteDates(Long memberId, Long meetingId, DateVoteRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);

        // 이미 확정됐거나 완료된 모임일 경우
        if (meeting.getStatus().isNotVotable()) {
            throw ErrorCode.MEETING_NOT_VOTABLE.toException();
        }

        // 모임 투표 상태 설정
        if (meeting.getStatus() == MeetingStatus.CREATED) {
            meeting.updateStatus(MeetingStatus.DATE_VOTING);
        }

        MeetingParticipant participant = meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND::toException);

        // 새로 투표한 날짜 목록
        List<LocalDate> dates = request.dates().stream().distinct().toList();

        // 내 기존 투표 내역
        List<DateVote> prevDateVotes = dateVoteRepository.findAllByMeetingParticipantId(participant.getId());

        // 기존 투표가 참조하던 DateOption id 목록
        List<Long> prevOptionIds = prevDateVotes.stream()
                .map(v -> v.getDateOption().getId())
                .distinct()
                .toList();

        // 내 기존 투표 삭제
        if (!prevDateVotes.isEmpty()) {
            dateVoteRepository.deleteAllInBatch(prevDateVotes);
        }

        // 투표 후보 매핑값 조회/생성
        Map<LocalDate, DateOption> optionMap = getOrCreateOptions(meeting, dates);

        // 새 투표 저장
        List<DateVote> votes = dates.stream()
                .map(d -> DateVote.builder()
                        .meetingParticipant(participant)
                        .dateOption(optionMap.get(d))
                        .build())
                .toList();

        dateVoteRepository.saveAll(votes);

        // 고아 투표 후보 제거
        if (!prevOptionIds.isEmpty()) {
            cleanupOrphanOptions(prevOptionIds);
        }
    }

    /**
     * 특정 모임에서 투표 수 기준 상위 날짜 목록을 조회한다.<br>
     * 동점일 경우 날짜 오름차순으로 정렬하여 반환한다.
     */
    @Transactional(readOnly = true)
    public VotedDatesResponse getTopDateOptions(Long memberId, Long meetingId, int limit) {
        validateMemberParticipation(memberId, meetingId);

        // 투표 수 기준 Top N, 동점이면 오름차순
        List<LocalDate> rankedByCount = dateOptionRepository.findRankedDatesByMeetingId(meetingId,
                PageRequest.of(0, limit));

        // 오름차순으로 정렬
        List<LocalDate> sortedByDate = rankedByCount.stream()
                .sorted()
                .toList();

        return new VotedDatesResponse(sortedByDate);
    }

    /**
     * 모임에서 실제로 투표가 발생한 모든 날짜를 조회한다.<br>
     * 캘린더 표시용으로 사용된다.
     */
    @Transactional(readOnly = true)
    public VotedDatesResponse getVotedDates(Long memberId, Long meetingId) {
        validateMemberParticipation(memberId, meetingId);

        List<LocalDate> votedDates = dateOptionRepository.findVotedDatesByMeetingId(meetingId);
        return new VotedDatesResponse(votedDates);
    }

    /**
     * 특정 날짜에 투표한 모임원 목록을 조회한다.<br>
     * 멤버 기본 정보만 반환하여 투표 현황 표시용으로 사용한다.
     */
    @Transactional(readOnly = true)
    public DateVotersResponse getVotersByDate(Long memberId, Long meetingId, LocalDate date) {
        validateMemberParticipation(memberId, meetingId);

        List<DateVote> votes = dateVoteRepository.findAllByMeetingIdAndDate(meetingId, date);

        List<DateVoterResponse> voters = votes.stream()
                .map(dv -> {
                    MeetingParticipant participant = dv.getMeetingParticipant();
                    Member member = participant.getMember();

                    return new DateVoterResponse(
                            member.getId(),
                            member.getNickname(),
                            member.getCharacterType()
                    );
                })
                .toList();
        return new DateVotersResponse(voters);
    }

    /**
     * 날짜 목록에 대해 날짜(LocalDate) -> 날짜 후보(DateOption)로 매핑하여 반환한다.<br>
     * 이미 존재하는 후보는 그대로 매핑하고, 새로운 날짜는 새로 생성/저장하여 map에 합친다.
     */
    private Map<LocalDate, DateOption> getOrCreateOptions(Meeting meeting, List<LocalDate> dates) {
        // 기존에 존재하는 날짜 후보
        List<DateOption> existing =
                dateOptionRepository.findAllByMeetingIdAndDateIn(meeting.getId(), dates);

        // 날짜 -> 투표 후보 매핑
        Map<LocalDate, DateOption> map = existing.stream()
                .collect(Collectors.toMap(DateOption::getDate, o -> o));

        // 추가될 날짜 후보
        List<DateOption> toCreate = dates.stream()
                .filter(d -> !map.containsKey(d))
                .map(d -> DateOption.builder()
                        .meeting(meeting)
                        .date(d)
                        .build())
                .toList();

        // 새로 추가된 투표 후보 없음
        if (toCreate.isEmpty()) {
            return map;
        }

        try {
            // 새로운 날짜 후보 저장
            List<DateOption> created = dateOptionRepository.saveAll(toCreate);
            created.forEach(o -> map.put(o.getDate(), o));
            return map;
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 날짜 투표를 할 경우(유니크 제약 조건) DB에서 최신값 조회하여 반환
            List<DateOption> after =
                    dateOptionRepository.findAllByMeetingIdAndDateIn(meeting.getId(), dates);
            return after.stream()
                    .collect(Collectors.toMap(DateOption::getDate, o -> o));
        }
    }

    /**
     * 어떤 투표에서도 사용되지 않는 고아 DateOption을 정리한다.<br>
     * 동시 투표로 인한 충돌 시에는 예외를 무시하고 정리를 건너뛴다.
     */
    private void cleanupOrphanOptions(List<Long> optionIds) {
        // 투표를 실제로 한 후보
        List<Long> stillUsed = dateVoteRepository.findExistingOptionIds(optionIds);
        HashSet<Long> stillUsedSet = new HashSet<>(stillUsed);

        // 제거할 고아값
        List<Long> toDelete = optionIds.stream()
                .filter(id -> !stillUsedSet.contains(id))
                .toList();

        if (toDelete.isEmpty()) {
            return;
        }

        try {
            dateOptionRepository.deleteAllByIdInBatch(toDelete);
        } catch (DataIntegrityViolationException e) {
            log.debug("Skip orphan DateOption cleanup due to concurrent usage. optionIds={}", toDelete);
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
