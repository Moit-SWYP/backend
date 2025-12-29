package pyws.swyp.meeting.service.vote;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.vote.DateVoteRequest;
import pyws.swyp.meeting.dto.vote.VoterResponse;
import pyws.swyp.meeting.dto.vote.VotersResponse;
import pyws.swyp.meeting.dto.vote.VotedDatesResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.vote.DateVote;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DateVoteService {

    private final DateVoteRepository dateVoteRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    /**
     * 모임원이 날짜 투표를 진행하며, 기존 투표가 존재할 경우 덮어쓴다.
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

        // 내 기존 투표 삭제
        dateVoteRepository.deleteAllByMeetingParticipantId(participant.getId());

        // 새로 투표한 날짜 목록
        List<LocalDate> dates = request.dates().stream().distinct().toList();

        // 새 투표 저장
        List<DateVote> votes = dates.stream()
                .map(d -> DateVote.builder()
                        .meeting(meeting)
                        .meetingParticipant(participant)
                        .date(d)
                        .build())
                .toList();

        dateVoteRepository.saveAll(votes);
    }

    /**
     * 특정 모임에서 가장 많은 투표를 받은 날짜 목록을 조회한다.<br>
     * 여러 날짜가 동점일 경우 날짜 오름차순으로 정렬하여 반환한다.
     */
    @Transactional(readOnly = true)
    public VotedDatesResponse getTopVotedDates(Long memberId, Long meetingId, int limit) {
        validateMemberParticipation(memberId, meetingId);

        // 투표 수 기준 Top N, 동점이면 오름차순
        List<LocalDate> dates = dateVoteRepository.findTopDatesByMeetingId(meetingId,
                PageRequest.of(0, limit));

        if (dates.isEmpty()) {
            throw ErrorCode.DATE_VOTE_NOT_FOUND.toException();
        }

        return new VotedDatesResponse(dates);
    }

    /**
     * 모임에서 실제로 투표가 발생한 모든 날짜를 조회한다.<br>
     * 캘린더 표시용으로 사용된다.
     */
    @Transactional(readOnly = true)
    public VotedDatesResponse getVotedDates(Long memberId, Long meetingId) {
        validateMemberParticipation(memberId, meetingId);

        List<LocalDate> votedDates = dateVoteRepository.findVotedDatesByMeetingId(meetingId);
        return new VotedDatesResponse(votedDates);
    }

    /**
     * 특정 날짜에 투표한 모임원 목록을 조회한다.<br>
     * 멤버 기본 정보만 반환하여 투표 현황 표시용으로 사용한다.
     */
    @Transactional(readOnly = true)
    public VotersResponse getVotersByDate(Long memberId, Long meetingId, LocalDate date) {
        validateMemberParticipation(memberId, meetingId);

        List<VoterResponse> voters = dateVoteRepository.findVotersByMeetingIdAndDate(meetingId, date);

        return new VotersResponse(voters);
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
