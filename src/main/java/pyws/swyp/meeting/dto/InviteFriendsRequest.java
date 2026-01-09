package pyws.swyp.meeting.dto;

import java.util.List;

public record InviteFriendsRequest(
        List<Long> friendIds
) {
}
