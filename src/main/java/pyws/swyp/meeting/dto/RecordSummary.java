package pyws.swyp.meeting.dto;

import java.util.List;

public record RecordSummary(
        Long meetingId,
        List<String> recordImageKeys,
        String recordContent
) {
}
