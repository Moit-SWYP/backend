package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.member.entity.CharacterType;

public record ParticipantResponse(
        Long memberId,
        String nickname,
        CharacterType characterType,
        ParticipantRole meetingParticipantRole
) {
}
