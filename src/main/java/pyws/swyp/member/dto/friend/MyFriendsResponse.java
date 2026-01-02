package pyws.swyp.member.dto.friend;

import java.util.List;

public record MyFriendsResponse(
        List<FriendInfo> friends
) {
}
