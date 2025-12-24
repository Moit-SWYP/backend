package pyws.swyp.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pyws.swyp.global.jwt.JwtProvider;
import pyws.swyp.member.entity.MemberRole;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new ServletException("Invalid JWT");
        }

        String accessToken = authorization.substring("Bearer ".length());

        Boolean isAccessToken = jwtProvider.validateToken(accessToken, true);
        if (isAccessToken) {
            Long memberId = jwtProvider.getMemberId(accessToken);
            MemberRole memberRole = jwtProvider.getRole(accessToken);

            var authority = new SimpleGrantedAuthority("ROLE_" + memberRole.name());
            var auth = new UsernamePasswordAuthenticationToken(memberId, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
