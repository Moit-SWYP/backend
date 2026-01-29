package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pyws.swyp.meeting.controller.api.MeetingApi;
import pyws.swyp.meeting.dto.MeetingBriefResponse;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.dto.MeetingCreateResponse;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;
import pyws.swyp.meeting.service.MeetingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController implements MeetingApi {

    private final MeetingService meetingService;

    @PostMapping
    public MeetingCreateResponse createMeeting(@AuthenticationPrincipal Long memberId, @RequestBody @Validated MeetingCreateRequest request) {
        return meetingService.createMeeting(memberId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMeeting(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        meetingService.deleteMeeting(memberId, id);
    }

    @DeleteMapping("/quit/{id}")
    public void quitMeeting(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        meetingService.quitMeeting(memberId, id);
    }

    @PatchMapping("/{id}")
    public void updateMeeting(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long id,
            @RequestBody MeetingUpdateRequest request
    ) {
        meetingService.updateMeeting(memberId, id, request);
    }

    @GetMapping("/waiting")
    public List<MeetingBriefResponse> getWaitingMeetings(@AuthenticationPrincipal Long memberId, @PageableDefault Pageable pageable) {
        return meetingService.getWaitingMeetings(memberId, pageable);
    }
}
