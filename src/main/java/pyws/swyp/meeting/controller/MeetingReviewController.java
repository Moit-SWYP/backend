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
import pyws.swyp.meeting.controller.api.MeetingReviewApi;
import pyws.swyp.meeting.dto.MeetingReviewCreate;
import pyws.swyp.meeting.dto.MeetingReviewResponse;
import pyws.swyp.meeting.service.MeetingReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/review")
public class MeetingReviewController implements MeetingReviewApi {

    private final MeetingReviewService meetingReviewService;

    @PostMapping
    public void create(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestBody @Validated MeetingReviewCreate request
    ) {
        meetingReviewService.create(memberId, meetingId, request);
    }

    @GetMapping
    public MeetingReviewResponse get(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return meetingReviewService.getMeetingReview(memberId, meetingId);
    }
}
