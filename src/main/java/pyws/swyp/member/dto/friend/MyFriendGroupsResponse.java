package pyws.swyp.member.dto.friend;

import java.util.List;

public record MyFriendGroupsResponse(
        List<FriendGroupInfo> friendGroups
) {
}
