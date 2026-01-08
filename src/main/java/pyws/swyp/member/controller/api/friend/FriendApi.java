package pyws.swyp.member.controller.api.friend;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import pyws.swyp.member.dto.friend.GroupCreateRequest;
import pyws.swyp.member.dto.friend.MyFriendGroupsResponse;
import pyws.swyp.member.dto.friend.MyFriendsResponse;

@SecurityRequirement(name = "auth")
@Tag(name = "Friend API")
public interface FriendApi {

    @Operation(
            summary = "친구 목록 조회",
            description =
                    """
                            친구 목록 조회 요청을 처리합니다.
                            친구는 한번이라도 같은 모임에 참여한 적이 있다면 자동으로 등록됩니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "친구 목록 조회 성공"
    )
    MyFriendsResponse getFriends(@AuthenticationPrincipal Long memberId);


    @Operation(
            summary = "친구 그룹 목록 조회",
            description =
                    """
                            친구 그룹 목록 조회 요청을 처리합니다
                            이는 모임에 친구를 초대할 때 간편하게 하기 위한 목적입니다.
                            그룹에 추가된 친구들에 대한 brief 정보를 함께 제공합니다. (memberId, characterType)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "친구 그룹 목록 조회 성공"
    )
    MyFriendGroupsResponse getFriendGroups(@AuthenticationPrincipal Long memberId);


    @Operation(
            summary = "친구 그룹 생성 성공",
            description =
                    """
                            그룹 생성 요청을 처리합니다.
                            name은 null & blank 불가하며
                            친구는 2명 이상이어야 등록됩니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "친구 그룹 생성 성공"
    )
    void createFriendGroup(@AuthenticationPrincipal Long memberId, @Validated @RequestBody GroupCreateRequest request);
}
