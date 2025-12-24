package pyws.swyp.member.dto;

import java.time.LocalDate;
import java.util.List;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.MemberRole;

public record MemberResponse(
        String email,
        String nickname,
        LocalDate birthDate,
        Gender gender,
        MemberRole memberRole,
        CharacterType characterType,
        List<SocialAccountInfo> socialAccounts
) {
}
