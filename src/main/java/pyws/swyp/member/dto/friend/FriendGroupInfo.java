package pyws.swyp.member.dto.friend;

import lombok.Builder;

import java.util.List;

@Builder
public record FriendGroupInfo(
        Long groupId,
        String name,
        List<FriendBriefInfo> friendsInGroup,
        Integer countFriend
) {
}
