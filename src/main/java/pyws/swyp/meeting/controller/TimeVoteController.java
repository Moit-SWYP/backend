package pyws.swyp.meeting.controller;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.TimeVoteApi;
import pyws.swyp.meeting.dto.vote.TimeVoteRequest;
import pyws.swyp.meeting.dto.vote.TimeVotersResponse;
import pyws.swyp.meeting.dto.vote.TopVotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VotedTimesResponse;
import pyws.swyp.meeting.service.vote.TimeVoteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/votes/times")
public class TimeVoteController implements TimeVoteApi {

    private final TimeVoteService timeVoteService;

    @PostMapping
    public void voteTimes(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestBody TimeVoteRequest request
    ) {
        timeVoteService.voteTimes(memberId, meetingId, request);
    }

    @GetMapping("/top")
    public TopVotedTimeResponse getTopVotedTime(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return timeVoteService.getTopVotedTime(memberId, meetingId);
    }

    @GetMapping
    public VotedTimesResponse getVotedTimesWithCounts(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return timeVoteService.getVotedTimesWithCounts(memberId, meetingId);
    }

    @GetMapping("/{time}/voters")
    public TimeVotersResponse getVotersByTime(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time
    ) {
        return timeVoteService.getVotersByTime(memberId, meetingId, time);
    }
}
