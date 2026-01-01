package pyws.swyp.meeting.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.MeetingConfirmApi;
import pyws.swyp.meeting.service.MeetingConfirmService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/votes")
public class MeetingConfirmController implements MeetingConfirmApi {

    private final MeetingConfirmService meetingConfirmService;

    @PostMapping("/date/confirm")
    public void confirmDateVote(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        meetingConfirmService.confirmDateVote(memberId, meetingId);
    }

    @PostMapping("/date/confirm/manual")
    public void confirmDateManual(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestParam LocalDate date
            ) {
        meetingConfirmService.confirmDate(memberId, meetingId, date);
    }

    @DeleteMapping("/date/confirm")
    public void cancelConfirmDate(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        meetingConfirmService.cancelConfirmDateVote(memberId, meetingId);
    }

    @PostMapping("/time/confirm")
    public void confirmTimeVote(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        meetingConfirmService.confirmTimeVote(memberId, meetingId);
    }

    @PostMapping("/time/confirm/manual")
    public void confirmTimeManual(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestParam LocalTime time
    ) {
        meetingConfirmService.confirmTime(memberId, meetingId, time);
    }

    @DeleteMapping("/time/confirm")
    public void cancelConfirmTime(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        meetingConfirmService.cancelConfirmTimeVote(memberId, meetingId);
    }
}
