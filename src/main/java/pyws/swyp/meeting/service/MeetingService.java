package pyws.swyp.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingBriefResponse;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    // 임시 조치
    private final MemberRepository memberRepository;

    public void createMeeting(Long memberId, MeetingCreateRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        Meeting meeting = request.toMeetingEntity();
        meetingRepository.save(meeting);

        MeetingParticipant meetingParticipant = MeetingParticipant.host(meeting, member);
        meetingParticipantRepository.save(meetingParticipant);
    }

    public void deleteMeeting(Long memberId, Long meetingId) {
        Meeting meeting = validActiveMeeting(meetingId);

        MeetingParticipant participant = validateMeetingParticipant(memberId, meetingId);
        if(participant.getParticipantRole() != pyws.swyp.meeting.entity.ParticipantRole.HOST) {
            throw ErrorCode.MEETING_ACCESS_DENIED.toException();
        }

        meeting.delete();
    }

    public void quitMeeting(Long memberId, Long meetingId) {
        validActiveMeeting(meetingId);

        MeetingParticipant participant = validateMeetingParticipant(memberId, meetingId);
        if(participant.getParticipantRole() != ParticipantRole.MEMBER) {
            throw ErrorCode.MEETING_QUIT_DENIED.toException();
        }

        meetingParticipantRepository.delete(participant);
    }

    public void updateMeeting(Long memberId, Long meetingId, MeetingUpdateRequest request) {
        Meeting meeting = validActiveMeeting(meetingId);
        validateMeetingParticipant(memberId, meetingId);

        meeting.update(request);
    }

    @Transactional(readOnly = true)
    public List<MeetingBriefResponse> getAllMeetings(Long memberId) {
        return meetingParticipantRepository.findMeetingsByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<MeetingBriefResponse> getWaitingMeetings(Long memberId, Pageable pageable) {
        List<MeetingStatus> statuses = List.of(MeetingStatus.CREATED, MeetingStatus.DATE_VOTING, MeetingStatus.PLACE_VOTING);
        return meetingParticipantRepository.findMeetingsByMemberIdAndStatus(memberId, statuses, pageable);
    }

    private Meeting validActiveMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .filter(Meeting::isActive)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);
    }

    private MeetingParticipant validateMeetingParticipant(Long memberId, Long meetingId) {
        return meetingParticipantRepository
                .findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_ACCESS_DENIED::toException);
    }

}
