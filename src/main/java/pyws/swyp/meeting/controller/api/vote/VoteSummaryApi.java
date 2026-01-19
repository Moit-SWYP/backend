package pyws.swyp.meeting.controller.api.vote;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import pyws.swyp.meeting.dto.vote.VoteSummary;

@SecurityRequirement(name = "auth")
@Tag(
        name = "Vote Summary API",
        description = "모임의 전체 투표 요약 정보를 조회하는 API"
)
public interface VoteSummaryApi {

    @Operation(
            summary = "모임 투표 요약 조회",
            description =
                    """
                    특정 모임의 전체 투표 현황을 요약 조회합니다.
                    
                    조회 정보:
                    - 모임 상태 (meetingStatus: VOTING / FIXED(일자 확정) / DONE(지난 모임))
                    - 호스트 여부 (isHost)
                    - 확정된 날짜 / 시간
                    - 날짜 투표 요약
                      - Top N 날짜
                      - 전체 투표 날짜 목록
                    - 시간 투표 요약
                      - Top N 시간
                      - 시간별 투표 수 목록
                    
                    Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 투표 요약 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "투표 요약 조회 성공",
                                    summary = "모임 투표 요약 정보",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "meetingStatus": "VOTING",
                                                "isHost": true,
                                                "confirmedDate": null,
                                                "confirmedTime": null,
                                                "dateSummary": {
                                                  "topDates": [
                                                    "2025-12-20",
                                                    "2025-12-21"
                                                  ],
                                                  "votedDates": [
                                                    "2025-12-20",
                                                    "2025-12-21",
                                                    "2025-12-22",
                                                    "2025-12-22"
                                                  ]
                                                },
                                                "timeSummary": {
                                                  "topTimes": [
                                                    "15:00",
                                                    "15:30"
                                                  ],
                                                  "votedTimes": [
                                                    {
                                                      "time": "15:00",
                                                      "count": 2
                                                    },
                                                    {
                                                      "time": "15:30",
                                                      "count": 2
                                                    },
                                                    {
                                                      "time": "16:00",
                                                      "count": 1
                                                    },
                                                    {
                                                      "time": "16:30",
                                                      "count": 1
                                                    }
                                                  ]
                                                }
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자"
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임을 찾을 수 없음"
    )
    VoteSummary getVoteSummary(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    description = "모임 ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long meetingId
    );
}

