package pyws.swyp.meeting.dto;

import java.util.List;

public record HomeResponse(
        List<MeetingBriefWithParticipantsResponse> homeMeetings,
        List<MeetingBriefResponse> waitingMeetings
) {
}
