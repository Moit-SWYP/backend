package pyws.swyp.meeting.dto.vote;

import java.util.List;

public record VotedTimesResponse(
        List<VotedTimeResponse> times
) {
}
