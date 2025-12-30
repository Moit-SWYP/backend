package pyws.swyp.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.entity.UuidFormatter;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.InvitationLinkResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.MemberRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MemberRepository memberRepository;

    public InvitationLinkResponse createInvitationLink(Long memberId, Long meetingId) {
        Meeting meeting = validActiveMeeting(meetingId);
        validateMeetingParticipant(memberId, meetingId);

        return new InvitationLinkResponse(
                meetingId,
                meeting.getPublicId().toString()
        );
    }

    public void joinMeetingFromLink(Long memberId, String inviteToken) {
        Meeting meeting = validActiveMeetingWithToken(inviteToken);

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );

        if (validateAlreadyJoined(memberId, meeting.getId())) {
            throw ErrorCode.MEETING_ALREADY_JOINED.toException();
        }

        MeetingParticipant participant = MeetingParticipant.member(meeting, member);
        meetingParticipantRepository.save(participant);
    }

    private Meeting validActiveMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .filter(Meeting::isActive)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);
    }

    private Meeting validActiveMeetingWithToken(String inviteToken) {
        String token = UuidFormatter.replaceUuid(inviteToken);
        return meetingRepository.findByPublicId(UUID.fromString(token))
                .filter(Meeting::isActive)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);
    }

    private boolean validateAlreadyJoined(Long memberId, Long meetingId) {
        return meetingParticipantRepository.existsByMemberIdAndMeetingId(memberId, meetingId);
    }

    private MeetingParticipant validateMeetingParticipant(Long memberId, Long meetingId) {
        return meetingParticipantRepository
                .findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_ACCESS_DENIED::toException);
    }

}
