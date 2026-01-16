package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pyws.swyp.meeting.controller.api.InvitationApi;
import pyws.swyp.meeting.dto.InvitationLinkResponse;
import pyws.swyp.meeting.dto.InviteFriendsRequest;
import pyws.swyp.meeting.service.InvitationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class InvitationController implements InvitationApi {

    private final InvitationService invitationService;

    @GetMapping("/{meetingId}/invitations/link")
    public InvitationLinkResponse createInvitationLink(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return invitationService.createInvitationLink(memberId, meetingId);
    }

    @PostMapping("/invitations/join")
    public void joinMeetingFromLink(@AuthenticationPrincipal Long memberId, @RequestParam String inviteToken) {
        invitationService.joinMeetingFromLink(memberId, inviteToken);
    }

    @PostMapping("/{meetingId}/invitations")
    public void inviteToMeetingFromFriends(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestBody InviteFriendsRequest request
    ) {
        invitationService.inviteToMeeting(memberId, meetingId, request);
    }
}
