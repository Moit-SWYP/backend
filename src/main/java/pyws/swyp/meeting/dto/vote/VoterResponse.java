package pyws.swyp.meeting.dto.vote;

import pyws.swyp.member.entity.CharacterType;

public record VoterResponse(
        Long memberId,
        String nickname,
        CharacterType characterType
) {
}
