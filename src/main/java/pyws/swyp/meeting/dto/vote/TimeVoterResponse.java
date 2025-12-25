package pyws.swyp.meeting.dto.vote;

import pyws.swyp.member.entity.CharacterType;

public record TimeVoterResponse(
        Long memberId,
        String nickname,
        CharacterType characterType
) {
}
