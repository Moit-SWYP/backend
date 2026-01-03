package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pyws.swyp.meeting.dto.MeetingReviewCreate;
import pyws.swyp.meeting.dto.MeetingReviewResponse;


@SecurityRequirement(name = "auth")
@Tag(name = "Meeting Review API", description = "모임 후기 작성 및 조회 API")
public interface MeetingReviewApi {

    @Operation(
            summary = "모임 후기 작성",
            description = """
                    모임 후기를 작성합니다.
                    
                    - 모임 상태가 DONE인 경우에만 작성할 수 있습니다.
                    - 동일 참여자는 후기 1개만 작성 가능합니다. (중복 작성 시 실패)
                    - imageKeys는 Object Storage에 업로드된 객체 key 목록입니다. (예: uploads/xxx.jpg)
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "후기 작성 성공")
    @ApiResponse(
            responseCode = "400",
            description = "종료되지 않은 모임",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "code": "MEET0010",
                      "message": "종료된 모임에만 후기를 작성할 수 있습니다."
                    }
                    """))
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 참여자 없음",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "code": "MEET0006",
                      "message": "존재하지 않는 모임원입니다."
                    }
                    """))
    )
    @ApiResponse(
            responseCode = "409",
            description = "이미 후기 존재",
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "code": "REV0001",
                      "message": "이미 해당 모임에 대한 후기가 존재합니다."
                    }
                    """))
    )
    @PostMapping("/api/meetings/{meetingId}/review")
    void create(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @RequestBody @Validated MeetingReviewCreate request
    );

    @Operation(
            summary = "내 모임 후기 조회",
            description = """
                    내 모임 후기를 조회합니다.
                    
                    - 모임 참여자만 조회할 수 있습니다.
                    - imageUrls는 private 객체 접근을 위한 presigned GET URL입니다. (만료 시간 존재)
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "후기 조회 성공")
    @ApiResponse(
            responseCode = "404",
            description = "리소스를 찾을 수 없음",
            content = @Content(
                    examples = {
                            @ExampleObject(
                                    name = "모임 참여자 없음",
                                    value = """
                                            {
                                                "code":"MEET0006",
                                                "message":"존재하지 않는 모임원입니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "후기 없음",
                                    value = """
                                            {
                                                "code":"REV0002",
                                                "message":"해당 모임에 대한 후기가 존재하지 않습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    @GetMapping("/api/meetings/{meetingId}/review")
    MeetingReviewResponse get(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );
}
