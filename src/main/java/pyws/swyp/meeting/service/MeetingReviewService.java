package pyws.swyp.meeting.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.infra.storage.service.ImagePresignService;
import pyws.swyp.meeting.dto.MeetingReviewCreate;
import pyws.swyp.meeting.dto.MeetingReviewResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingReview;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.ReviewImage;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingReviewRepository;
import pyws.swyp.meeting.repository.ReviewImageRepository;

@Service
@RequiredArgsConstructor
public class MeetingReviewService {

    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingReviewRepository meetingReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ImagePresignService imagePresignService;

    @Transactional
    public void create(Long memberId, Long meetingId, MeetingReviewCreate request) {
        MeetingParticipant participant = meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND::toException);

        MeetingStatus status = participant.getMeeting().getStatus();

        if (status != MeetingStatus.DONE) {
            throw ErrorCode.MEETING_NOT_DONE.toException();
        }

        MeetingReview meetingReview = MeetingReview.builder()
                .meetingParticipant(participant)
                .content(request.content())
                .build();

        try {
            meetingReviewRepository.save(meetingReview);
        } catch (DataIntegrityViolationException e) {
            throw ErrorCode.REVIEW_ALREADY_EXISTS.toException();
        }

        // 이미지 저장
        List<String> imageKeys = request.imageKeys();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            int order = 0;
            List<ReviewImage> reviewImages = new ArrayList<>();

            for (String key : imageKeys) {
                reviewImages.add(ReviewImage.builder()
                        .meetingReview(meetingReview)
                        .imageKey(key)
                        .sortOrder(order++)
                        .build());
            }

            reviewImageRepository.saveAll(reviewImages);
        }
    }

    @Transactional(readOnly = true)
    public MeetingReviewResponse getMeetingReview(Long memberId, Long meetingId) {
        MeetingParticipant participant = meetingParticipantRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND::toException);
        Meeting meeting = participant.getMeeting();

        MeetingReview meetingReview = meetingReviewRepository.findByMeetingParticipantId(participant.getId())
                .orElseThrow(ErrorCode.REVIEW_NOT_FOUND::toException);

        List<String> imageKeys = reviewImageRepository.findImageKeysByReviewId(meetingReview.getId());

        List<String> imageUrls = imageKeys.stream()
                .map(imagePresignService::generatePresignedGetUrl)
                .toList();

        return new MeetingReviewResponse(
                meeting.getDate(),
                meeting.getTime(),
                meeting.getTitle(),
                3,  // todo courseCount 임시
                meetingReview.getContent(),
                imageUrls
        );
    }
}
