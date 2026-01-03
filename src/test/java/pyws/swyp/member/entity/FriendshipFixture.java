package pyws.swyp.member.entity;

import org.springframework.test.util.ReflectionTestUtils;
import pyws.swyp.member.entity.friend.FriendGroup;
import pyws.swyp.member.entity.friend.FriendGroupMember;

public class FriendshipFixture {

    public static Member createMember(Long id) {
        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    public static FriendGroup createGroup(Long id, String name) {
        FriendGroup group = FriendGroup.builder()
                .name(name)
                .build();

        ReflectionTestUtils.setField(group, "id", id);
        return group;
    }

    public static FriendGroupMember createGroupMember(FriendGroup group, Member friend) {
        return FriendGroupMember.builder()
                .group(group)
                .member(friend)
                .build();
    }

}
