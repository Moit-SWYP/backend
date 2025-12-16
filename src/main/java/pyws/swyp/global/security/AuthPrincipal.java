package pyws.swyp.global.security;

import pyws.swyp.member.entity.Role;

public record AuthPrincipal(
        Long memberId,
        Role role
) {
}
