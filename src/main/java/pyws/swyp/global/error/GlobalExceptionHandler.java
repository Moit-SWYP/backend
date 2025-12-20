package pyws.swyp.global.error;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException.Reference;
import tools.jackson.databind.exc.MismatchedInputException;

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
            String fieldName = mie.getPath().getLast().getPropertyName();
            String message = "값의 형식이 올바르지 않습니다.";
            error.put(fieldName, message);
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
}
