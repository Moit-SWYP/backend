package pyws.swyp.member.dto;

import java.time.LocalDate;
import java.util.List;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Role;

public record MemberResponse(
        String email,
        String nickname,
        LocalDate birthDate,
        Gender gender,
        Role role,
        List<SocialAccountInfo> socialAccounts
) {
}
