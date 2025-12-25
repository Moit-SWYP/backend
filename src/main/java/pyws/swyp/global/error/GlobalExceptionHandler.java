package pyws.swyp.global.error;


import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.MismatchedInputException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 요청 DTO 검증 예외 (@Valid, @Validated)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse exceptionHandler(MethodArgumentNotValidException exception) {
        return new ErrorResponse(ErrorCode.INVALID_INPUT, exception.getFieldErrors());
    }

    /**
     * 요청 Body 역직렬화 실패 예외
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();

        if (cause instanceof MismatchedInputException mie) {
            Map<String, String> error = new HashMap<>();
            String path = toPathString(mie);
            error.put(path, "값의 형식이 올바르지 않습니다.");
            return new ErrorResponse(ErrorCode.INVALID_INPUT, error);
        }

        return new ErrorResponse(ErrorCode.INVALID_INPUT);
    }

    /**
     * @PathVariable / @RequestParam 타입 변환 실패 예외
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String field = e.getName();
        Object valueObj = e.getValue();
        String value = (valueObj == null) ? "null" : valueObj.toString();
        String message = "값의 형식이 올바르지 않습니다. (입력값: " + value + ")";

        Map<String, String> error = new HashMap<>();
        error.put(field, message);
        return new ErrorResponse(ErrorCode.INVALID_INPUT, error);
    }

    /**
     * 비즈니스 로직 예외
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode));
    }

    /**
     * 처리되지 않은 서버 내부 오류에 대한 최종 처리
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(Exception e, HttpServletRequest request) {
        log.error(
                "[INTERNAL_SERVER_ERROR] path={} method={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                e.getMessage(),
                e
        );
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String toPathString(MismatchedInputException mie) {
        StringBuilder sb = new StringBuilder();

        for (var ref : mie.getPath()) {
            if (ref.getPropertyName() != null) {
                if (!sb.isEmpty()) sb.append(".");
                sb.append(ref.getPropertyName());
            } else if (ref.getIndex() >= 0) {
                sb.append("[").append(ref.getIndex()).append("]");
            }
        }

        return sb.isEmpty() ? "unknown" : sb.toString();
    }
}
