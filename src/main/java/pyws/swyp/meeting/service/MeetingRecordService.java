package pyws.swyp.meeting.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.infra.storage.service.ImagePresignService;
import pyws.swyp.meeting.dto.MeetingRecordCreate;
import pyws.swyp.meeting.dto.MeetingRecordResponse;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingRecord;
import pyws.swyp.meeting.entity.MeetingStatus;
import pyws.swyp.meeting.entity.RecordImage;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRecordRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.RecordImageRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MeetingRecordService {

    private final MemberRepository memberRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MeetingRecordRepository meetingRecordRepository;
    private final RecordImageRepository recordImageRepository;
    private final ImagePresignService imagePresignService;

    @Transactional
    public void createMeetingRecord(Long memberId, Long meetingId, MeetingRecordCreate request) {
        if (!meetingParticipantRepository.existsByMemberIdAndMeetingId(memberId, meetingId)) {
            throw ErrorCode.MEETING_PARTICIPANT_NOT_FOUND.toException();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(ErrorCode.MEETING_NOT_FOUND::toException);
        MeetingStatus status = meeting.getStatus();

        if (status != MeetingStatus.DONE) {
            throw ErrorCode.MEETING_NOT_DONE.toException();
        }

        MeetingRecord meetingRecord = MeetingRecord.builder()
                .member(member)
                .meeting(meeting)
                .content(request.content())
                .build();

        try {
            meetingRecordRepository.save(meetingRecord);
        } catch (DataIntegrityViolationException e) {
            throw ErrorCode.RECORD_ALREADY_EXISTS.toException();
        }

        // 이미지 저장
        List<String> imageKeys = request.imageKeys();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            int order = 0;
            List<RecordImage> recordImages = new ArrayList<>();

            for (String key : imageKeys) {
                recordImages.add(RecordImage.builder()
                        .meetingRecord(meetingRecord)
                        .imageKey(key)
                        .sortOrder(order++)
                        .build());
            }

            recordImageRepository.saveAll(recordImages);
        }
    }

    /**
     * 내가 참여한 특정 모임의 후기를 조회한다.
     */
    @Transactional(readOnly = true)
    public MeetingRecordResponse getMeetingRecord(Long memberId, Long meetingId) {
        if (!meetingParticipantRepository.existsByMemberIdAndMeetingId(memberId, meetingId)) {
            throw ErrorCode.MEETING_PARTICIPANT_NOT_FOUND.toException();
        }

        MeetingRecord meetingRecord = meetingRecordRepository.findByMemberIdAndMeetingId(memberId, meetingId)
                .orElseThrow(ErrorCode.RECORD_NOT_FOUND::toException);

        Meeting meeting = meetingRecord.getMeeting();

        List<String> imageKeys = recordImageRepository.findImageKeysByRecordId(meetingRecord.getId());
        List<String> imageUrls = imageKeys.stream()
                .map(imagePresignService::generatePresignedGetUrl)
                .toList();

        return new MeetingRecordResponse(
                meeting.getDate(),
                meeting.getTime(),
                meeting.getTitle(),
                3,  // todo: courseCount 임시
                meetingRecord.getContent(),
                imageUrls
        );
    }
}
