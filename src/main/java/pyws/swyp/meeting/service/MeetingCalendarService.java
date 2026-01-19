package pyws.swyp.meeting.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pyws.swyp.infra.storage.service.ImagePresignService;
import pyws.swyp.meeting.dto.MonthlyMeetingSummary;
import pyws.swyp.meeting.dto.RecordContentRow;
import pyws.swyp.meeting.dto.RecordImageRow;
import pyws.swyp.meeting.dto.RecordSummary;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.repository.MeetingRecordRepository;
import pyws.swyp.meeting.repository.MeetingRepository;

@Service
@RequiredArgsConstructor
public class MeetingCalendarService {

    private final MeetingRepository meetingRepository;
    private final ImagePresignService imagePresignService;
    private final MeetingRecordRepository meetingRecordRepository;

    public List<MonthlyMeetingSummary> getMonthlyMeetings(Long memberId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        List<Meeting> meetings = meetingRepository.findMyMeetingsByDateBetween(memberId, start, end);
        if (meetings.isEmpty()) {
            return List.of();
        }

        List<Long> meetingIds = meetings.stream()
                .map(Meeting::getId)
                .toList();

        // todo: 코스 개수 집계

        // meetingId -> recordImages
        Map<Long, List<String>> imageKeyMap = meetingRecordRepository.findMyRecordImagesTop3(meetingIds, memberId)
                .stream()
                .collect(Collectors.groupingBy(
                        RecordImageRow::meetingId,
                        LinkedHashMap::new,
                        Collectors.mapping(RecordImageRow::imageKey, Collectors.toList())
                ));

        // meetingId -> recordContent
        Map<Long, String> contentMap = meetingRecordRepository.findMyRecordContents(meetingIds, memberId)
                .stream()
                .collect(Collectors.toMap(
                        RecordContentRow::meetingId,
                        RecordContentRow::recordContent,
                        (a, b) -> a
                ));

        // meetingId -> RecordSummary
        Map<Long, RecordSummary> recordMap = new HashMap<>();
        for (Long meetingId : meetingIds) {
            List<String> imageKeys = imageKeyMap.getOrDefault(meetingId, List.of());
            String content = contentMap.get(meetingId);

            recordMap.put(meetingId, new RecordSummary(meetingId, imageKeys, content));
        }

        return meetings.stream()
                .map(meeting -> {
                    RecordSummary record = recordMap.get(meeting.getId());

                    List<String> imageUrls = new ArrayList<>();
                    if (record != null && !record.recordImageKeys().isEmpty()) {
                        imageUrls = record.recordImageKeys().stream()
                                .map(imagePresignService::generatePresignedGetUrl)
                                .toList();
                    }

                    String recordContent = (record == null) ? null : record.recordContent();

                    // todo: 코스 임시 값
                    int courseCount = 3;
                    boolean isCourseVoteDone = true;

                    return new MonthlyMeetingSummary(
                            meeting.getId(),
                            meeting.getTitle(),
                            meeting.getDate(),
                            meeting.getDate().getDayOfWeek(),
                            meeting.getTime(),
                            meeting.getType(),
                            isCourseVoteDone,
                            courseCount,
                            imageUrls,
                            recordContent
                    );
                })
                .toList();
    }
}
