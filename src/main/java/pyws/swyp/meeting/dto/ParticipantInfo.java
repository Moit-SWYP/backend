package pyws.swyp.meeting.dto;

import pyws.swyp.meeting.entity.ParticipantRole;
import pyws.swyp.member.entity.CharacterType;

public record ParticipantInfo(
        Long memberId,
        String nickname,
        CharacterType characterType,
        ParticipantRole meetingParticipantRole
) {
}
