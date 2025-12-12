package pyws.swyp.global.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import pyws.swyp.global.error.ErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final HttpServletRequest httpServletRequest;

    /**
     * beforeBodyWrite 실행 여부를 결정하는 메서드
     *
     * 여기서 false를 반환하면 컨트롤러 응답이 그대로 나가고,
     * true를 반환하면 beforeBodyWrite()가 호출되어 공통 응답을 감싸게 됨.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        String path = httpServletRequest.getRequestURI();

        // Swagger 제외
        if (path.startsWith("/swagger") ||
                path.startsWith("/v3") ||
                path.startsWith("/error")) {
            return false;
        }

        // String 응답은 공통 응답 포맷 없이 그대로 반환
        if (String.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 공통 응답 중복 래핑 방지
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType,
                                            MediaType selectedContentType,
                                            Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                            ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null) {
            return ApiResponse.success(null);
        }

        // 예외일 경우 그대로 반환
        if (ErrorResponse.class.isAssignableFrom(body.getClass())) {
            return body;
        }

        return ApiResponse.success(body);
    }
}
