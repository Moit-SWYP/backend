package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pyws.swyp.meeting.dto.InvitationLinkResponse;
import pyws.swyp.meeting.dto.InviteFriendsRequest;

@SecurityRequirement(name = "auth")
@Tag(name = "Invitation API", description = "모임 초대 API")
public interface InvitationApi {

    @Operation(
            summary = "모임 초대 링크 생성에 필요한 토큰 조회",
            description =
                    """
                            모임 초대 토큰 조회에 성공합니다.
                            Meeting의 UUID 타입의 public_id를 반환합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 초대 토큰 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 초대 토큰 조회 성공",
                                    value = """
                                            {
                                                "code": "SUCCESS",
                                                "message": "요청이 성공적으로 처리되었습니다.",
                                                "data": {
                                                    "meetingId": 2,
                                                    "inviteToken": "c2d76392-e5aa-11f0-b3bd-0242ac130002"
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
                                                "message": "존재하지 않는 모임입니다"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "모임원 아님",
                                    value = """
                                            {
                                                "code": "MEET0006",
                                                "message": "존재하지 않는 모임원입니다"
                                            }
                                            """
                            )
                    }
            )
    )
    InvitationLinkResponse createInvitationLink(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    );

    @Operation(
            summary = "초대 링크로 모임 참여",
            description =
                    """
                            초대 링크에 있는 토큰을 통해 모임에 참여합니다.
                            String 타입의 토큰을 UUID로 변환하고 그에 맞는 meeting에 meetingParticipant를 추가합니다.
                            이미 참여한 유저이거나 유효하지 않은 meeting 이라면 에러를 반환합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "초대 링크로 모임 참여 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "초대 링크로 모임 참여 성공",
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
            responseCode = "409",
            description = "이미 참여한 모임",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "이미 참여한 모임 실패",
                                    value = """
                                            {
                                                "code": "MEET0007",
                                                "message": "이미 참여중인 모임입니다."
                                            }
                                            """
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "모임 정보를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                                            {
                                                "code": "MEET0001",
                                                "message": "존재하지 않는 모임입니다"
                                            }
                                            """
                            )
                    }
            )
    )
    void joinMeetingFromLink(@AuthenticationPrincipal Long memberId, @RequestParam String inviteToken);

    @Operation(
            summary = "친구 리스트에서 모임 초대",
            description =
                    """
                            친구 목록에서 초대할 멤버들을 선택해 리스트로 요청합니다.
                            초대된 멤버들을 MEMBER로 모임에 추가합니다.
                            멤버 구성원들 중 친구가 아닌 사람이 있다면 친구에 양방향으로 추가합니다.
                            이미 친구라면 metCount를 증가시킵니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "모임 초대 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "모임 초대 성공",
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
            responseCode = "404",
            description = "존재하지 않은 멤버를 초대하려 함.",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "추가하려는 멤버가 존재하지 않음",
                                    value = """
                                            {
                                                "code": "MEM0002",
                                                "message": "존재하지 않는 멤버가 포함되어 있습니다"
                                            }
                                            """
                            )
                    }
            )
    )
    void inviteToMeetingFromFriends(@AuthenticationPrincipal Long memberId, @PathVariable Long meetingId, @RequestBody InviteFriendsRequest request);
}
