package pyws.swyp.meeting.controller.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.vote.VoteSummaryApi;
import pyws.swyp.meeting.dto.vote.VoteSummary;
import pyws.swyp.meeting.service.vote.VoteSummaryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings/{meetingId}/votes/summary")
public class VoteSummaryController implements VoteSummaryApi {

    private final VoteSummaryService voteSummaryService;

    @GetMapping
    public VoteSummary getVoteSummary(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long meetingId
    ) {
        return voteSummaryService.getVoteSummary(memberId, meetingId);
    }
}
