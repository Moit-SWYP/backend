package pyws.swyp.meeting.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.MeetingCalendarApi;
import pyws.swyp.meeting.dto.MonthlyMeetingSummary;
import pyws.swyp.meeting.service.MeetingCalendarService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingCalendarController implements MeetingCalendarApi {

    private final MeetingCalendarService meetingCalendarService;

    @GetMapping("/monthly")
    public List<MonthlyMeetingSummary> getMonthly(
            @AuthenticationPrincipal Long memberId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return meetingCalendarService.getMonthlyMeetings(memberId, year, month);
    }
}
