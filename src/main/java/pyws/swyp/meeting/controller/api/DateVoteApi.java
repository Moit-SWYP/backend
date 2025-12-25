package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pyws.swyp.meeting.dto.vote.DateVoteRequest;
import pyws.swyp.meeting.dto.vote.DateVotersResponse;
import pyws.swyp.meeting.dto.vote.VotedDatesResponse;

@SecurityRequirement(name = "auth")
@Tag(name = "Date Vote API", description = "모임 날짜 투표 / 투표 현황 조회 API")
public interface DateVoteApi {

    @Operation(
            summary = "날짜 투표",
            description =
                    """
                            모임원이 날짜 투표를 진행합니다.
                            
                            - 요청으로 전달된 날짜 목록으로 투표가 저장됩니다. (중복 날짜는 제거됩니다.)
                            - 기존에 내가 투표했던 날짜는 모두 삭제되고, 새 목록으로 다시 저장됩니다. (replace 방식)
                            - 모임 상태가 CREATED인 경우 DATE_VOTING으로 변경될 수 있습니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "날짜 투표 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "날짜 투표 성공",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "투표 불가능한 모임 상태",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "투표 불가",
                                    value = """
                                            {
                                              "code": "MEETING_NOT_VOTABLE",
                                              "message": "투표할 수 없는 모임 상태입니다."
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 또는 모임원(참가자) 정보를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                                            {
                                              "code": "MEETING_NOT_FOUND",
                                              "message": "모임을 찾을 수 없습니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "모임 참가자 아님",
                                    value = """
                                            {
                                              "code": "MEETING_PARTICIPANT_NOT_FOUND",
                                              "message": "모임 참가자를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    void voteDates(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @RequestBody @Validated DateVoteRequest dateVoteRequest
    );

    @Operation(
            summary = "상위 날짜 조회",
            description =
                    """
                            투표 수 기준으로 상위 날짜를 조회합니다.
                            
                            - limit 만큼만 반환합니다. (기본값: 3)
                            - 동점인 경우 날짜 오름차순으로 정렬됩니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "상위 날짜 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "상위 날짜 조회 성공",
                                    summary = "Top 3 날짜",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "dates": ["2025-01-10", "2025-01-11", "2025-01-13"]
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 참가자 아님",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 참가자 아님",
                                    value = """
                                            {
                                              "code": "MEETING_PARTICIPANT_NOT_FOUND",
                                              "message": "모임 참가자를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    VotedDatesResponse getTopDates(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(
                    name = "limit",
                    description = "조회할 최대 개수 (기본값 3)",
                    example = "3"
            )
            @RequestParam(defaultValue = "3") int limit
    );

    @Operation(
            summary = "투표된 날짜 전체 조회",
            description =
                    """
                            모임에서 실제로 투표가 발생한 날짜 목록을 조회합니다.
                            
                            - 캘린더에서 '투표된 날짜' 마킹 용도로 사용합니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "투표된 날짜 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "투표된 날짜 조회 성공",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "dates": ["2025-01-10", "2025-01-11", "2025-01-12"]
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 참가자 아님",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 참가자 아님",
                                    value = """
                                            {
                                              "code": "MEETING_PARTICIPANT_NOT_FOUND",
                                              "message": "모임 참가자를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    VotedDatesResponse getVotedDates(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "특정 날짜 투표자 조회",
            description =
                    """
                            특정 날짜에 투표한 모임원 목록을 조회합니다.
                            
                            - 날짜는 PathVariable로 전달됩니다. (yyyy-MM-dd)
                            - 투표 현황 화면에서 날짜별 참여자 확인 용도로 사용합니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            
                            예) /api/meetings/1/votes/dates/2025-01-10/voters
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "특정 날짜 투표자 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "특정 날짜 투표자 조회 성공",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "voters": [
                                                  {
                                                    "memberId": 1,
                                                    "nickname": "스윕유저1",
                                                    "characterType": "ACTIVE"
                                                  },
                                                  {
                                                    "memberId": 2,
                                                    "nickname": "스윕유저2",
                                                    "characterType": "HEALER"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 참가자 아님",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 참가자 아님",
                                    value = """
                                            {
                                              "code": "MEETING_PARTICIPANT_NOT_FOUND",
                                              "message": "모임 참가자를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    DateVotersResponse getVotersByDate(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(
                    name = "date",
                    description = "조회할 날짜 (yyyy-MM-dd)",
                    example = "2025-01-10"
            )
            @PathVariable
            @DateTimeFormat(iso = ISO.DATE) LocalDate date
    );
}
