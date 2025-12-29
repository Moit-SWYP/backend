package pyws.swyp.meeting.dto.vote;

import java.util.List;

public record VotersResponse(
        List<VoterResponse> voters
) {
}
