package pyws.swyp.meeting.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.entity.UuidFormatter;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.dto.InvitationLinkResponse;
import pyws.swyp.meeting.dto.InviteFriendsRequest;
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

        // Todo: 기존 모임원들에게 OO이 들어왔다는 푸시 알림 발행
    }

    public void inviteToMeeting(Long memberId, Long meetingId, InviteFriendsRequest request) {
        // meeting, participant 검증
        Meeting meeting = validActiveMeeting(meetingId);
        validateMeetingParticipant(memberId, meetingId);

        // friend memberId 존재 여부 검증
        validateMembersExist(request.friendIds());

        // 이미 존재하는 참여자 memberId 조회
        List<Long> existingIds = meetingParticipantRepository
                .findOtherMemberIdsByMeetingId(memberId, meetingId);

        // memberIds -> MeetingParticipant로 변환
        List<MeetingParticipant> participants = request.friendIds().stream()
                .filter(id -> !existingIds.contains(id))
                .map(id -> MeetingParticipant.member(
                        meeting,
                        entityManager.getReference(Member.class, id)
                ))
                .toList();

        if(participants.isEmpty()) return;

        // 저장 시 DB 제약 에러 예외 처리
        try {
            meetingParticipantRepository.saveAll(participants);
        } catch (DataIntegrityViolationException e) {
            throw ErrorCode.INVALID_INVITE_MEMBER.toException();
        }

        // 성공하면 친구 맺기
        makeFriends(meetingId);

        // Todo: 초대받은 사용자들에게 푸시 알림
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

    private void validateMembersExist(List<Long> memberIds) {
        List<Member> members = memberRepository.findAllById(memberIds);

        if (members.size() != memberIds.size()) {
            throw ErrorCode.INVALID_INVITE_MEMBER.toException();
        }
    }

    /**
     * 같은 모임에 참여한 사람들과 자동으로 친구 맺기
     */
    private void makeFriends(Long memberId, Long meetingId) {

        // 모임 참여자 리스트 확인
        List<Long> friendIds = meetingParticipantRepository.findOtherMemberIdsByMeetingId(memberId, meetingId);
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

    private void makeFriends(Long meetingId) {
        List<Long> memberIds = meetingParticipantRepository.findMemberIdsByMeetingId(meetingId);

        // 이미 존재하는 친구 관계 조회
        List<Friendship> existing = friendshipRepository.findByMemberIds(memberIds);
        Map<Pair<Long, Long>, Friendship> existingMap = existing.stream()
                .collect(Collectors.toMap(
                        f -> Pair.of(
                                f.getMember().getId(),
                                f.getFriend().getId()
                        ),
                        Function.identity()
                ));

        List<Friendship> newFriendships = new ArrayList<>();
        for (int i = 0; i < memberIds.size(); i++) {
            for (int j = i+1; j < memberIds.size(); j++) {
                Long a = memberIds.get(i);
                Long b = memberIds.get(j);

                Pair<Long, Long> ab = Pair.of(a, b);
                Pair<Long, Long> ba = Pair.of(b, a);

                // 이미 친구라면 metCount 증가
                if (existingMap.containsKey(ab)) {
                    existingMap.get(ab).increaseMetCount();
                    existingMap.get(ba).increaseMetCount();
                    continue;
                }

                // 아니라면 친구 양방향 생성
                Member aRef = entityManager.getReference(Member.class, a);
                Member bRef = entityManager.getReference(Member.class, b);

                newFriendships.add(Friendship.builder()
                        .member(aRef)
                        .friend(bRef)
                        .build()
                );
                newFriendships.add(Friendship.builder()
                        .member(bRef)
                        .friend(aRef)
                        .build()
                );
            }
        }

        friendshipRepository.saveAll(newFriendships);
    }

}
