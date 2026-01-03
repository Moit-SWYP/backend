package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import pyws.swyp.meeting.dto.MonthlyMeetingSummary;

@SecurityRequirement(name = "auth")
@Tag(name = "Meeting Calendar API", description = "모임 캘린더 조회 API")
public interface MeetingCalendarApi {

    @Operation(
            summary = "월간 모임 캘린더 조회",
            description = """
                    특정 연/월에 해당하는 모임 요약 목록을 조회합니다.
                    
                    - year, month는 필수 쿼리 파라미터입니다.
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "월간 모임 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = MonthlyMeetingSummary.class)),
                    examples = @ExampleObject(
                            name = "success",
                            summary = "성공 응답 예시 (공통 응답 래퍼 포함)",
                            value = """
                                    {
                                      "code": "SUCCESS",
                                      "message": "요청이 성공적으로 처리되었습니다.",
                                      "data": [
                                        {
                                          "meetingId": 1,
                                          "title": "신년 모임",
                                          "date": "2026-01-10",
                                          "dayOfWeek": "SATURDAY",
                                          "time": "18:30",
                                          "dateVoteDone": true,
                                          "timeVoteDone": true,
                                          "courseVoteDone": false,
                                          "courseCount": 3,
                                          "hasReview": true,
                                          "reviewContent": "오랜만에 다 같이 만나서 정말 즐거웠어요!",
                                          "reviewImageUrl": "https://cdn.moit.shop/reviews/meeting-1.jpg"
                                        },
                                        {
                                          "meetingId": 2,
                                          "title": "스터디 정기 모임",
                                          "date": "2026-01-25",
                                          "dayOfWeek": "SUNDAY",
                                          "time": "14:00",
                                          "dateVoteDone": true,
                                          "timeVoteDone": false,
                                          "courseVoteDone": false,
                                          "courseCount": 0,
                                          "hasReview": false,
                                          "reviewContent": null,
                                          "reviewImageUrl": null
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    List<MonthlyMeetingSummary> getMonthly(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Long memberId,

            @Parameter(description = "연도", example = "2026", required = true)
            @RequestParam int year,

            @Parameter(description = "월(1~12)", example = "1", required = true)
            @RequestParam int month
    );
}
