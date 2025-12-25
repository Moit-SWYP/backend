package pyws.swyp.meeting.dto.vote;

import pyws.swyp.member.entity.CharacterType;

public record DateVoterResponse(
        Long memberId,
        String nickname,
        CharacterType characterType
) {
}
