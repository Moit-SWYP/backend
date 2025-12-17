package pyws.swyp.config;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 테스트 환경에서 @AuthenticationPrincipal Long memberId 파라미터를
 * 동적으로 주입하기 위한 설정 클래스
 */
@TestConfiguration
public class AuthPrincipalTestConfig implements WebMvcConfigurer {

    /**
     * 테스트 전용 ArgumentResolver 등록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new DynamicMemberIdResolver());
    }

    /**
     * @AuthenticationPrincipal Long memberId 파라미터에
     * 테스트에서 생성한 회원 ID를 동적으로 주입
     */
    static class DynamicMemberIdResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(
                    org.springframework.security.core.annotation.AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(Long.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            Long memberId = AuthTestPrincipalContext.getMemberId();
            if (memberId == null) {
                return 1L;
            }
            return memberId;
        }
    }
}
