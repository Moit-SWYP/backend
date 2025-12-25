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
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pyws.swyp.meeting.dto.vote.DateVoteRequest;
import pyws.swyp.meeting.dto.vote.DateVotersResponse;
import pyws.swyp.meeting.dto.vote.TimeVoteRequest;
import pyws.swyp.meeting.dto.vote.TimeVotersResponse;
import pyws.swyp.meeting.dto.vote.TopVotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VotedDatesResponse;
import pyws.swyp.meeting.dto.vote.VotedTimesResponse;

@SecurityRequirement(name = "auth")
@Tag(name = "Time Vote API", description = "모임 시간 투표 / 투표 현황 조회 API")
public interface TimeVoteApi {

    @Operation(
            summary = "시간 투표",
            description =
                    """
                            모임원이 특정 날짜에 대해 시간 투표를 진행합니다.
                            
                            - 요청으로 전달된 시간 목록으로 투표가 저장됩니다. (중복 시간 제거)
                            - 기존에 내가 투표했던 시간은 모두 삭제되고, 새 목록으로 다시 저장됩니다. (replace 방식)
                            - 시간은 LocalTime 형식(HH:mm)으로 전달됩니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "시간 투표 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "시간 투표 성공",
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
            description = "잘못된 요청 또는 투표 불가능한 모임 상태",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "잘못된 요청",
                                    value = """
                        {
                          "code": "COM0001",
                          "message": "잘못된 요청입니다."
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "투표 불가 상태",
                                    value = """
                        {
                          "code": "MEET0005",
                          "message": "이미 확정됐거나 완료된 모임입니다."
                        }
                        """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 또는 모임원 정보를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                        {
                          "code": "MEET0001",
                          "message": "존재하지 않는 모임입니다."
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "모임원 아님",
                                    value = """
                        {
                          "code": "MEET0006",
                          "message": "존재하지 않는 모임원입니다."
                        }
                        """
                            )
                    }
            )
    )
    void voteTimes(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @RequestBody @Validated TimeVoteRequest request
    );

    @Operation(
            summary = "최다 득표 시간 조회",
            description =
                    """
                            투표 수 기준으로 가장 많이 득표된 시간을 조회합니다.
                            
                            - 동점인 경우 시간 오름차순으로 하나만 반환됩니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "최다 득표 시간 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "최다 득표 시간 조회 성공",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "time": "15:00"
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 또는 모임원 정보를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                        {
                          "code": "MEET0001",
                          "message": "존재하지 않는 모임입니다."
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "모임원 아님",
                                    value = """
                        {
                          "code": "MEET0006",
                          "message": "존재하지 않는 모임원입니다."
                        }
                        """
                            )
                    }
            )
    )
    TopVotedTimeResponse getTopVotedTime(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "투표된 시간 및 득표 수 조회",
            description =
                    """
                            모임에서 실제로 투표된 시간과 각 시간의 득표 수를 조회합니다.
                            
                            - 시간 투표 현황 화면에서 사용됩니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "투표된 시간 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "투표된 시간 조회 성공",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "times": [
                                                  { "time": "15:00", "count": 3 },
                                                  { "time": "16:00", "count": 1 }
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
            description = "모임 또는 모임원 정보를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                        {
                          "code": "MEET0001",
                          "message": "존재하지 않는 모임입니다."
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "모임원 아님",
                                    value = """
                        {
                          "code": "MEET0006",
                          "message": "존재하지 않는 모임원입니다."
                        }
                        """
                            )
                    }
            )
    )
    VotedTimesResponse getVotedTimesWithCounts(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "특정 시간 투표자 조회",
            description =
                    """
                            특정 시간에 투표한 모임원 목록을 조회합니다.
                            
                            - 시간은 PathVariable로 전달됩니다. (HH:mm)
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            
                            예) /api/meetings/1/votes/times/15:00/voters
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "특정 시간 투표자 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "특정 시간 투표자 조회 성공",
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
            description = "모임 또는 모임원 정보를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                        {
                          "code": "MEET0001",
                          "message": "존재하지 않는 모임입니다."
                        }
                        """
                            ),
                            @ExampleObject(
                                    name = "모임원 아님",
                                    value = """
                        {
                          "code": "MEET0006",
                          "message": "존재하지 않는 모임원입니다."
                        }
                        """
                            )
                    }
            )
    )
    TimeVotersResponse getVotersByTime(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "모임 ID", example = "1")
            @PathVariable Long meetingId,

            @Parameter(
                    name = "time",
                    description = "조회할 시간 (HH:mm)",
                    example = "15:00"
            )
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time
    );
}
