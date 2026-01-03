package pyws.swyp.meeting.service;

import jakarta.persistence.EntityManager;
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
import pyws.swyp.member.entity.friend.Friendship;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.friend.FriendshipRepository;
import pyws.swyp.member.repository.MemberRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;

    private final EntityManager entityManager;

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

        makeFriends(memberId, meeting.getId());
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

    /**
     * 같은 모임에 참여한 사람들과 자동으로 친구 맺기
     */
    private void makeFriends(Long memberId, Long meetingId) {

        // 모임 참여자 리스트 확인
        List<Long> friendIds = meetingParticipantRepository.findMemberIdsByMeetingId(memberId, meetingId);
        if (friendIds.isEmpty()) return;

        // 기존 친구 관계 확인
        List<Friendship> existingFriends
                = friendshipRepository.findByMemberIdAndFriendIds(memberId, friendIds);
        Map<Long, Friendship> existingFriendshipMap = existingFriends.stream()
                .collect(Collectors.toMap(
                        f -> f.getFriend().getId(),
                        Function.identity()
                ));

        // 새로운 친구 저장
        List<Friendship> newFriendships = new ArrayList<>();

        for(Long friendId : friendIds) {
            Friendship existing = existingFriendshipMap.get(friendId);
            if (existing != null) {
                existing.increaseMetCount();
                continue;
            }

            Member memberRef = entityManager.getReference(Member.class, memberId);
            Member friendRef = entityManager.getReference(Member.class, friendId);

            newFriendships.add(
                    Friendship.builder()
                            .member(memberRef)
                            .friend(friendRef)
                            .build()
            );

            newFriendships.add(
                    Friendship.builder()
                            .member(friendRef)
                            .friend(memberRef)
                            .build()
            );
        }

        friendshipRepository.saveAll(newFriendships);
    }

}
