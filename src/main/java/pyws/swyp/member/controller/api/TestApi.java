package pyws.swyp.member.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pyws.swyp.member.dto.TestRequest;
import pyws.swyp.member.dto.TestResponse;

@Tag(name = "Member API")
@RequestMapping("/api/members")
public interface TestApi {

    @Operation(
            summary = "성공 테스트",
            description = "테스트 설명"
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "SuccessResponse",
                            value = """
                                    {
                                      "code": "200",
                                      "message": "요청이 성공적으로 처리되었습니다.",
                                      "data": {
                                        "message": "서버에서 받은 메시지: string",
                                        "timestamp": "2025-12-04T17:46:42.765593100"
                                      }
                                    }
                                    """
                    )
            )
    )
    @PostMapping("/test-success")
    TestResponse test(@RequestBody TestRequest request);

    @Operation(summary = "실패 테스트", description = "테스트 설명")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "404",
                    description = "비즈니스 로직 예외",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MemberNotFound",
                                    value = """
                {
                  "code": "404",
                  "message": "존재하지 않는 회원입니다."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "@Validated 유효성 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    value = """
                {
                  "code": "400",
                  "message": "잘못된 요청입니다.",
                  "data": {
                    "name": "name 은 필수입니다."
                  }
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/test-fail")
    TestResponse test2(@RequestBody TestRequest request);
}
