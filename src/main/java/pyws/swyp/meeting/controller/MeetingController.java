package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pyws.swyp.meeting.controller.api.MeetingApi;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.service.MeetingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController implements MeetingApi {

    private final MeetingService meetingService;

    @PostMapping
    public void createMeeting(@RequestBody @Validated MeetingCreateRequest request) {
        meetingService.createMeeting(request);
    }

    @DeleteMapping("/{id}")
    public void deleteMeeting(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        meetingService.deleteMeeting(memberId, id);
    }

    @DeleteMapping("quit/{id}")
    public void quitMeeting(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        meetingService.quitMeeting(memberId, id);
    }
}
