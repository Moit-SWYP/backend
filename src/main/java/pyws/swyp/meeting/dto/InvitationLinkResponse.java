package pyws.swyp.meeting.dto;

public record InvitationLinkResponse(
        Long meetingId,
        String inviteToken
) {
}
