package pyws.swyp.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
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

        MeetingParticipant meetingParticipant = MeetingParticipant.builder()
                .meeting(meeting)
                .member(member)
                .role("HOST")
                .build();
        meetingParticipantRepository.save(meetingParticipant);
    }
}
