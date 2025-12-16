package pyws.swyp.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pyws.swyp.global.security.AuthPrincipal;
import pyws.swyp.member.entity.Role;

@TestConfiguration
public class AuthPrincipalTestConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new FixedAuthPrincipalResolver(
                new AuthPrincipal(1L, Role.MEMBER)
        ));
    }

    @RequiredArgsConstructor
    static class FixedAuthPrincipalResolver implements HandlerMethodArgumentResolver {

        private final AuthPrincipal principal;

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(
                    org.springframework.security.core.annotation.AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(AuthPrincipal.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return principal;
        }
    }
}
