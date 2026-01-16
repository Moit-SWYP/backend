package pyws.swyp.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MeetingRecordResponse(
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,
        String meetingTitle,
        int courseCount,
        String content,
        List<String> imageUrls
) {
}
