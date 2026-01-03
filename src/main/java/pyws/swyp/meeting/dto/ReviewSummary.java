package pyws.swyp.meeting.dto;

public record ReviewSummary(
        Long meetingId,
        boolean hasReview,
        String reviewImageKey,
        String reviewContent
) {
}
