package pyws.swyp.member.service.friend;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pyws.swyp.member.dto.friend.FriendGroupInfo;
import pyws.swyp.member.dto.friend.GroupCreateRequest;
import pyws.swyp.member.dto.friend.MyFriendGroupsResponse;
import pyws.swyp.member.dto.friend.MyFriendsResponse;
import pyws.swyp.member.entity.FriendshipFixture;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.friend.FriendGroup;
import pyws.swyp.member.entity.friend.FriendGroupMember;
import pyws.swyp.member.entity.friend.Friendship;
import pyws.swyp.member.repository.friend.FriendGroupMemberRepository;
import pyws.swyp.member.repository.friend.FriendGroupRepository;
import pyws.swyp.member.repository.friend.FriendshipRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendServiceTest {
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private FriendGroupRepository friendGroupRepository;
    @Mock
    private FriendGroupMemberRepository friendGroupMemberRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private FriendService friendService;

    @Test
    @DisplayName("내 친구 목록 조회에 성공한다.")
    void 친구_목록_조회_성공() {
        // given
        Long memberId = 1L;
        Member me = FriendshipFixture.createMember(memberId);
        Member friend1 = FriendshipFixture.createMember(2L);
        Member friend2 = FriendshipFixture.createMember(3L);

        Friendship friendship1 = Friendship.builder()
                .member(me)
                .friend(friend1)
                .build();

        Friendship friendship2 = Friendship.builder()
                .member(me)
                .friend(friend2)
                .build();

        when(friendshipRepository.findByMemberId(memberId))
                .thenReturn(List.of(friendship1, friendship2));

        // when
        MyFriendsResponse response = friendService.getFriends(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.friends()).hasSize(2);

        verify(friendshipRepository, times(1)).findByMemberId(memberId);
    }

    @Test
    @DisplayName("그룹 목록 조회에 성공한다.")
    void 친구_그룹_목록_조회_성공() {
        // given
        Long memberId = 1L;

        FriendGroup group1 = FriendshipFixture.createGroup(1L, "스위프");
        FriendGroup group2 = FriendshipFixture.createGroup(2L, "기요미들");
        FriendGroup group3 = FriendshipFixture.createGroup(3L, "모잉이들");

        when(friendGroupRepository.findByMemberId(memberId))
                .thenReturn(List.of(group1, group2, group3));

        Member friend1 = FriendshipFixture.createMember(2L);
        Member friend2 = FriendshipFixture.createMember(3L);
        Member friend3 = FriendshipFixture.createMember(4L);
        Member friend4 = FriendshipFixture.createMember(5L);
        Member friend5 = FriendshipFixture.createMember(6L);
        Member friend6 = FriendshipFixture.createMember(7L);

        FriendGroupMember m1 = FriendshipFixture.createGroupMember(group1, friend1);
        FriendGroupMember m2 = FriendshipFixture.createGroupMember(group1, friend2);
        FriendGroupMember m3 = FriendshipFixture.createGroupMember(group2, friend3);
        FriendGroupMember m4 = FriendshipFixture.createGroupMember(group2, friend4);
        FriendGroupMember m5 = FriendshipFixture.createGroupMember(group3, friend5);
        FriendGroupMember m6 = FriendshipFixture.createGroupMember(group3, friend6);

        when(friendGroupMemberRepository.findByFriendGroupIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(m1, m2, m3, m4, m5, m6));

        // when
        MyFriendGroupsResponse response = friendService.getFriendGroups(memberId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.friendGroups()).hasSize(3);

        FriendGroupInfo groupInfo1 = response.friendGroups().stream()
                .filter(g -> g.groupId().equals(1L))
                .findFirst()
                .orElseThrow();

        assertThat(groupInfo1.name()).isEqualTo("스위프");
        assertThat(groupInfo1.countFriend()).isEqualTo(2);
        assertThat(groupInfo1.friendsInGroup()).hasSize(2);

        FriendGroupInfo groupInfo3 = response.friendGroups().stream()
                .filter(g -> g.groupId().equals(3L))
                .findFirst()
                .orElseThrow();

        assertThat(groupInfo3.name()).isEqualTo("모잉이들");
        assertThat(groupInfo3.countFriend()).isEqualTo(2);
        assertThat(groupInfo3.friendsInGroup()).hasSize(2);
    }

    @Test
    @DisplayName("친구 그룹 생성 성공")
    void 친구_그룹_생성_성공() {
        // given
        Long ownerId = 1L;
        List<Long> friendIds = List.of(2L, 3L);
        String groupName = "모잉이들";

        GroupCreateRequest request = new GroupCreateRequest(
                groupName,
                friendIds
        );

        Member ownerRef = mock(Member.class);
        Member friendRef1 = mock(Member.class);
        Member friendRef2 = mock(Member.class);

        FriendGroup savedGroup = FriendGroup.builder()
                .name(groupName)
                .build();

        when(entityManager.getReference(Member.class, ownerId))
                .thenReturn(ownerRef);
        when(entityManager.getReference(Member.class, 2L))
                .thenReturn(friendRef1);
        when(entityManager.getReference(Member.class, 3L))
                .thenReturn(friendRef2);

        when(friendGroupRepository.save(any(FriendGroup.class)))
                .thenReturn(savedGroup);

        // when
        friendService.createFriendGroup(ownerId, request);

        // then
        // 그룹 저장 검증
        ArgumentCaptor<FriendGroup> groupCaptor = ArgumentCaptor.forClass(FriendGroup.class);

        verify(friendGroupRepository, times(1))
                .save(groupCaptor.capture());

        FriendGroup group = groupCaptor.getValue();
        assertThat(group.getGroupName()).isEqualTo(groupName);
        assertThat(group.getOwner()).isEqualTo(ownerRef);

        // 그룹 멤버 저장 검증
        ArgumentCaptor<List<FriendGroupMember>> groupMemberCaptor = ArgumentCaptor.forClass(List.class);

        verify(friendGroupMemberRepository, times(1))
                .saveAll(groupMemberCaptor.capture());

        List<FriendGroupMember> savedMembers = groupMemberCaptor.getValue();
        assertThat(savedMembers).hasSize(2);

        assertThat(savedMembers).allMatch(m -> m.getFriendGroup() == savedGroup);

        verify(entityManager).getReference(Member.class, ownerId);
        verify(entityManager).getReference(Member.class, 2L);
        verify(entityManager).getReference(Member.class, 3L);
    }
}
