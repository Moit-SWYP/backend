package pyws.swyp.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.validation.FieldError;

@JsonPropertyOrder({"code", "message", "data"})
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final Map<String, String> data;

    public ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = new HashMap<>();
        fieldErrors.forEach(error -> this.data.put(error.getField(), error.getDefaultMessage()));
    }

    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.data = null;  //NON_EMPTY 때문에 null이면 JSON에 노출되지 않음
    }
}

