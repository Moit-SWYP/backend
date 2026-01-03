package pyws.swyp.meeting.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.infra.storage.service.ImagePresignService;
import pyws.swyp.meeting.dto.MonthlyMeetingSummary;
import pyws.swyp.meeting.dto.ReviewSummary;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.MeetingReviewRepository;

@Service
@RequiredArgsConstructor
public class MeetingCalendarService {

    private final MeetingRepository meetingRepository;
    private final ImagePresignService imagePresignService;
    private final MeetingReviewRepository meetingReviewRepository;

    public List<MonthlyMeetingSummary> getMonthlyMeetings(Long memberId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        List<Meeting> meetings = meetingRepository.findByDateBetweenOrderByDateAscTimeAsc(start, end);
        if (meetings.isEmpty()) {
            throw ErrorCode.MEETING_NOT_FOUND_IN_MONTH.toException();
        }

        List<Long> meetingIds = meetings.stream()
                .map(Meeting::getId)
                .toList();

        // todo 코스 개수 집계

        // meetingId -> ReviewSummary 매핑
        Map<Long, ReviewSummary> reviewMap = meetingReviewRepository.findMyReviewSummaryByMeetingIds(meetingIds,
                        memberId)
                .stream()
                .collect(Collectors.toMap(
                        ReviewSummary::meetingId,
                        r -> r
                ));

        return meetings.stream()
                .map(meeting -> {
                    ReviewSummary review = reviewMap.get(meeting.getId());
                    boolean hasReview = review != null && review.hasReview();

                    String imageUrl = null;
                    if (hasReview && review.reviewImageKey() != null) {
                        imageUrl = imagePresignService.generatePresignedGetUrl(review.reviewImageKey());
                    }

                    int courseCount = 3;  // 임시 값

                    boolean isDateVoteDone = meeting.getDate() != null;
                    boolean isTimeVoteDone = meeting.getTime() != null;

                    boolean isCourseVoteDone = true;

                    return new MonthlyMeetingSummary(
                            meeting.getId(),
                            meeting.getTitle(),
                            meeting.getDate(),
                            meeting.getDate().getDayOfWeek().name(),
                            meeting.getTime(),
                            isDateVoteDone,
                            isTimeVoteDone,
                            isCourseVoteDone,
                            courseCount,
                            hasReview,
                            imageUrl,
                            hasReview ? review.reviewContent() : null
                    );
                })
                .toList();
    }
}
