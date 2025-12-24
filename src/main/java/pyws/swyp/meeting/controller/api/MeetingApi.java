package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import pyws.swyp.meeting.dto.MeetingBriefResponse;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;

import java.util.List;

@SecurityRequirement(name = "auth")
@Tag(name = "Meeting API")
public interface MeetingApi {

    @Operation(
            summary = "모임 생성",
            description =
                    """
                            모임 생성 요청을 처리합니다.
                            모임 생성 시 생성한 사람을 모임의 HOST로 지정하여 저장합니다.
                            title은 null & blank 불가합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 생성 성공"
    )
    void createMeeting(@AuthenticationPrincipal Long memberId, @RequestBody @Validated MeetingCreateRequest request);

    @Operation(
            summary = "모임 삭제",
            description =
                    """
                            모임 삭제 요청을 처리합니다.
                            해당 모임의 HOST가 요청해야만 삭제 요청을 정상처리합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 삭제 성공"
    )
    void deleteMeeting(@AuthenticationPrincipal Long memberId, @PathVariable Long id);

    @Operation(
            summary = "모임 탈퇴",
            description =
                    """
                            모임 탈퇴 및 거절 요청을 처리합니다.
                            해당 모임의 MEMBER인 경우 탈퇴 요청을 정상처리합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 탈퇴 성공"
    )
    void quitMeeting(@AuthenticationPrincipal Long memberId, @PathVariable Long id);

    @Operation(
            summary = "모임 수정",
            description =
                    """
                            모임 수정 요청을 처리합니다.
                            모임 구성원 누구든 수정 요청할 경우 정상처리합니다.
                            title은 blank 일 수 없습니다.(null 가능)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 수정 성공"
    )
    void updateMeeting(@AuthenticationPrincipal Long memberId,
            @PathVariable Long id,
            @RequestBody MeetingUpdateRequest request
    );

    @Operation(
            summary = "전체 모임 리스트 조회",
            description =
                    """
                            캘린더뷰에서 필요한 전체 모임 리스트 조회 요청을 처리합니다.
                            본인이 속한 모임들에 대한 brief 모임 정보를 가져옵니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "전체 모임 리스트 조회 성공"
    )
    List<MeetingBriefResponse> getAllMeetings(@AuthenticationPrincipal Long memberId);

    @Operation(
            summary = "기다리고 있는 모임 리스트 조회",
            description =
                    """
                            "친구들이 기다려요" -> 더보기 클릭 시 요청한 모임 리스트 조회를 처리합니다.
                            Meeting.Status의 값이 CREATED, DATE_VOTING, PLACE_VOTING인 경우만 반환합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "전체 모임 리스트 조회 성공"
    )
    List<MeetingBriefResponse> getWaitingMeetings(@AuthenticationPrincipal Long memberId, @PageableDefault Pageable pageable);
}
