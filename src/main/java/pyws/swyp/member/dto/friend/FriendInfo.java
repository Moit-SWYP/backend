package pyws.swyp.member.dto.friend;

import lombok.Builder;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.friend.Friendship;
import pyws.swyp.member.entity.Member;

@Builder
public record FriendInfo(
        Long memberId,
        String nickname,
        CharacterType characterType,
        Integer metCount
) {
    public static FriendInfo from (Friendship friendship) {
        Member friend = friendship.getFriend();

        return FriendInfo.builder()
                .memberId(friend.getId())
                .nickname(friend.getNickname())
                .characterType(friend.getCharacterType())
                .metCount(friendship.getMetCount())
                .build();
    }
}
