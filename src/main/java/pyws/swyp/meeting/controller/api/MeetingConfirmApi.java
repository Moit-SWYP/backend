package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@SecurityRequirement(name = "auth")
@Tag(name = "Meeting Confirm API", description = "모임 날짜/시간 확정 및 확정 취소 API")
public interface MeetingConfirmApi {

    @Operation(
            summary = "날짜 투표 결과로 확정",
            description = """
                    모임의 날짜 투표 결과를 기반으로 날짜를 확정합니다.
                    
                    - 최다 득표한 날짜 후보들 중 가장 과거 날짜로 확정됩니다.
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "날짜 확정 성공")
    void confirmDateVote(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "날짜 수동 확정",
            description = """
                    모임장이 특정 날짜를 직접 지정하여 모임 날짜를 확정합니다.
                    
                    - 날짜 형식: yyyy-MM-dd
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "날짜 수동 확정 성공")
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (날짜 형식 오류 등)",
            content = @Content(examples = @ExampleObject(value = """
                    {"code":"COM0001","message":"잘못된 요청입니다.","data":{"date":"값의 형식이 올바르지 않습니다. (입력값: 2025-13-40)"}}
                    """))
    )
    void confirmDateManual(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(description = "확정할 날짜", example = "2025-12-20")
            @RequestParam LocalDate date
    );

    @Operation(
            summary = "확정된 날짜 취소",
            description = """
                    확정된 모임 날짜를 취소합니다.
                    
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "날짜 확정 취소 성공")
    void cancelConfirmDate(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "시간 투표 결과로 확정",
            description = """
                    모임의 시간 투표 결과를 기반으로 시간을 확정합니다.
                    
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "시간 확정 성공")
    void confirmTimeVote(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "시간 수동 확정",
            description = """
                    호스트가 특정 시간을 직접 지정하여 모임 시간을 확정합니다.
                    
                    - 시간 형식: HH:mm
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "시간 수동 확정 성공")
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (시간 형식 오류 등)",
            content = @Content(examples = @ExampleObject(value = """
                    {"code":"COM0001","message":"잘못된 요청입니다.","data":{"time":"값의 형식이 올바르지 않습니다. (입력값: 99:99)"}}
                    """))
    )
    void confirmTimeManual(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(description = "확정할 시간", example = "15:00")
            @RequestParam LocalTime time
    );

    @Operation(
            summary = "확정된 시간 취소",
            description = """
                    확정된 모임 시간을 취소합니다.
                    
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "시간 확정 취소 성공")
    void cancelConfirmTime(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );
}
