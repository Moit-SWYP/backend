package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.MeetingApi;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.service.MeetingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController implements MeetingApi {

    private final MeetingService meetingService;

    // 모임 생성
    @PostMapping
    public void createMeeting(@RequestBody @Validated MeetingCreateRequest request) {
        meetingService.createMeeting(request);
    }
}
