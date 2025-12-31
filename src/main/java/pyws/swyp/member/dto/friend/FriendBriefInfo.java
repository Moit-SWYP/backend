package pyws.swyp.member.dto.friend;

import lombok.Builder;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.friend.FriendGroupMember;
import pyws.swyp.member.entity.Member;

@Builder
public record FriendBriefInfo(
        Long memberId,
        CharacterType characterType
) {

    public static FriendBriefInfo from (FriendGroupMember friendGroupMember) {
        Member friend = friendGroupMember.getMember();

        return FriendBriefInfo.builder()
                .memberId(friend.getId())
                .characterType(friend.getCharacterType())
                .build();
    }
}
