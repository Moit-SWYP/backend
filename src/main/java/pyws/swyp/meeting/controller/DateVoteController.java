package pyws.swyp.meeting.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.DateVoteApi;
import pyws.swyp.meeting.dto.vote.DateVoteRequest;
import pyws.swyp.meeting.dto.vote.DateVotersResponse;
import pyws.swyp.meeting.dto.vote.VotedDatesResponse;
import pyws.swyp.meeting.service.vote.DateVoteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/votes/dates")
public class DateVoteController implements DateVoteApi {

    private final DateVoteService dateVoteService;

    @PostMapping
    public void voteDates(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestBody @Validated DateVoteRequest dateVoteRequest
    ) {
        dateVoteService.voteDates(memberId, meetingId, dateVoteRequest);
    }

    @GetMapping("/top")
    public VotedDatesResponse getTopDates(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "3") int limit
    ) {
        return dateVoteService.getTopDateOptions(memberId, meetingId, limit);
    }

    @GetMapping
    public VotedDatesResponse getVotedDates(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return dateVoteService.getVotedDates(memberId, meetingId);
    }

    @GetMapping("/{date}/voters")
    public DateVotersResponse getVotersByDate(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId,
            @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date
    ) {
        return dateVoteService.getVotersByDate(memberId, meetingId, date);
    }
}
