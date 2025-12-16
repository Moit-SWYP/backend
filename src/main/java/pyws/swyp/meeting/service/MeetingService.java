package pyws.swyp.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.Role;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    // 임시 조치
    private final MemberRepository memberRepository;

    public void createMeeting(MeetingCreateRequest request) {
        // Todo: security 구현 되는대로 멤버 파싱 적용.
        // 임시 조치
        Member member = memberRepository.findById(1L).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        Meeting meeting = request.toMeetingEntity();
        meetingRepository.save(meeting);

        MeetingParticipant meetingParticipant = MeetingParticipant.host(meeting, member);
        meetingParticipantRepository.save(meetingParticipant);
    }

    public void deleteMeeting(Long memberId, Long meetingId) {
        Meeting meeting = validActiveMeeting(meetingId);
        validateMeetingHostPermission(memberId, meetingId);

        meeting.delete();
    }

    private Meeting validActiveMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .filter(Meeting::isActive)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);
    }

    private void validateMeetingHostPermission(Long memberId, Long meetingId) {
        meetingParticipantRepository
                .findRoleByMeetingIdAndMemberId(memberId, meetingId)
                .filter(role -> role == Role.HOST)
                .orElseThrow(ErrorCode.MEETING_ACCESS_DENIED::toException);
    }
}
