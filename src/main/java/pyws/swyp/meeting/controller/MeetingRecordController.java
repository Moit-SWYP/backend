package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.MeetingRecordApi;
import pyws.swyp.meeting.dto.MeetingRecordCreate;
import pyws.swyp.meeting.dto.MeetingRecordResponse;
import pyws.swyp.meeting.service.MeetingRecordService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/records")
public class MeetingRecordController implements MeetingRecordApi {

    private final MeetingRecordService meetingRecordService;

    @PostMapping
    public void createMeetingRecord(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestBody @Validated MeetingRecordCreate request
    ) {
        meetingRecordService.createMeetingRecord(memberId, meetingId, request);
    }

    @GetMapping
    public MeetingRecordResponse getMeetingRecord(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return meetingRecordService.getMeetingRecord(memberId, meetingId);
    }
}
