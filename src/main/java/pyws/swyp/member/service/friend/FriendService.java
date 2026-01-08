package pyws.swyp.member.service.friend;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.member.dto.friend.GroupCreateRequest;
import pyws.swyp.member.dto.friend.*;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.friend.FriendGroup;
import pyws.swyp.member.entity.friend.FriendGroupMember;
import pyws.swyp.member.entity.friend.Friendship;
import pyws.swyp.member.repository.friend.FriendGroupMemberRepository;
import pyws.swyp.member.repository.friend.FriendGroupRepository;
import pyws.swyp.member.repository.friend.FriendshipRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final FriendGroupMemberRepository friendGroupMemberRepository;

    private final EntityManager entityManager;

    public MyFriendsResponse getFriends(Long memberId) {
        List<Friendship> friendships = friendshipRepository.findByMemberId(memberId);

        List<FriendInfo> friends = friendships.stream()
                        .map(FriendInfo::from)
                        .toList();

        return new MyFriendsResponse(friends);
    }

    public MyFriendGroupsResponse getFriendGroups(Long memberId) {
        /*
          생성한 그룹 조회
         */
        List<FriendGroup> friendGroups = friendGroupRepository.findByMemberId(memberId);

        if (friendGroups.isEmpty()) {
            return MyFriendGroupsResponse.empty();
        }

        List<Long> friendGroupIds = friendGroups.stream()
                .map(BaseEntity::getId)
                .toList();

        /*
         * 그룹에 맞는 그룹 내 친구 목록 조회
         * 그룹ID, 그룹 내 친구 리스트 로 맵핑
         */
        List<FriendGroupMember> friendGroupMembers = friendGroupMemberRepository.findByFriendGroupIds(friendGroupIds);

        Map<Long, List<FriendGroupMember>> groupedByGroupId = friendGroupMembers.stream()
                .collect(Collectors.groupingBy(
                        fgm -> fgm.getFriendGroup().getId()
                ));

        /*
         * groupedByGroupId를 FriendGroupInfo 형태로 다듬기
         * 이를 기반으로 MyFriendGroupsResponse 생성
         */
        List<FriendGroupInfo> friendGroupInfos =
                groupedByGroupId.entrySet().stream()
                        .map(entry -> {
                            Long groupId = entry.getKey();
                            List<FriendGroupMember> members = entry.getValue();

                            FriendGroup group = members.getFirst().getFriendGroup();

                            List<FriendBriefInfo> friendsInGroup =
                                    members.stream()
                                            .map(FriendBriefInfo::from)
                                            .toList();

                            return FriendGroupInfo.builder()
                                    .groupId(groupId)
                                    .name(group.getGroupName())
                                    .friendsInGroup(friendsInGroup)
                                    .countFriend(friendsInGroup.size())
                                    .build();
                        }).toList();

        return new MyFriendGroupsResponse(friendGroupInfos);
    }

    public void createFriendGroup(Long memberId, GroupCreateRequest request) {
        // 이미 있는 그룹인지 확인

        // 그룹 생성
        Member memberRef = entityManager.getReference(Member.class, memberId);
        FriendGroup group = FriendGroup.builder()
                .owner(memberRef)
                .name(request.name())
                .build();

        FriendGroup savedGroup = friendGroupRepository.save(group);

        // 그룹 내 구성원 추가
        List<FriendGroupMember> groupMembers = request.groupMemberIds().stream()
                .map(gmId -> FriendGroupMember.builder()
                        .group(savedGroup)
                        .member(entityManager.getReference(Member.class, gmId))
                        .build()
                ).toList();

        friendGroupMemberRepository.saveAll(groupMembers);
    }
}
