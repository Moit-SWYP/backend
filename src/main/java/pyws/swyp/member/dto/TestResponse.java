package pyws.swyp.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TestResponse {

    private String message;
    private String timestamp;
}
