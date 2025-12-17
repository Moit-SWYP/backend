package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import pyws.swyp.meeting.dto.MeetingCreateRequest;

@SecurityRequirement(name = "auth")
@Tag(name = "Meeting API")
public interface MeetingApi {

    @Operation(
            summary = "모임 생성",
            description =
                    """
                            모임 생성 요청을 처리합니다.
                            모임 생성 시 생성한 사람을 모임의 HOST로 지정하여 저장합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 생성 성공"
    )
    void createMeeting(@RequestBody @Validated MeetingCreateRequest request);
}
