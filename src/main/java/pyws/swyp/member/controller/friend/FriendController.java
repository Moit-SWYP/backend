package pyws.swyp.member.controller.friend;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pyws.swyp.member.controller.api.friend.FriendApi;
import pyws.swyp.member.dto.friend.GroupCreateRequest;
import pyws.swyp.member.dto.friend.MyFriendGroupsResponse;
import pyws.swyp.member.dto.friend.MyFriendsResponse;
import pyws.swyp.member.service.friend.FriendService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class FriendController implements FriendApi {
    private final FriendService friendService;

    @GetMapping("/friendships")
    public MyFriendsResponse getFriends(@AuthenticationPrincipal Long memberId) {
        return friendService.getFriends(memberId);
    }

    @GetMapping("/groups")
    public MyFriendGroupsResponse getFriendGroups(@AuthenticationPrincipal Long memberId) {
        return friendService.getFriendGroups(memberId);
    }

    @PostMapping("/groups")
    public void createFriendGroup(@AuthenticationPrincipal Long memberId, @Validated @RequestBody GroupCreateRequest request) {
        friendService.createFriendGroup(memberId, request);
    }
}
