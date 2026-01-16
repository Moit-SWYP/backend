package pyws.swyp.meeting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.dto.RecordContentRow;
import pyws.swyp.meeting.dto.RecordImageRow;
import pyws.swyp.meeting.entity.MeetingRecord;

public interface MeetingRecordRepository extends JpaRepository<MeetingRecord, Integer> {

    @Query("""
            select new pyws.swyp.meeting.dto.RecordContentRow(
                m.id,
                mr.content
            )
            from Meeting m
            join MeetingParticipant mp
                    on mp.meeting = m
                   and mp.member.id = :memberId
            left join MeetingRecord mr
                    on mr.member.id = :memberId
                   and mr.meeting = m
            where m.id in :meetingIds
            """)
    List<RecordContentRow> findMyRecordContents(List<Long> meetingIds, Long memberId);

    @Query("""
            select new pyws.swyp.meeting.dto.RecordImageRow(
                ri.meetingRecord.meeting.id,
                ri.imageKey,
                ri.sortOrder
            )
            from RecordImage ri
            where ri.meetingRecord.meeting.id in :meetingIds
              and ri.sortOrder <= 2
              and ri.meetingRecord.member.id = :memberId
            order by ri.meetingRecord.meeting.id asc, ri.sortOrder asc
            """)
    List<RecordImageRow> findMyRecordImagesTop3(List<Long> meetingIds, Long memberId);

    @Query("""
            select r.id
            from MeetingRecord r
            where r.member.id = :memberId
            """)
    List<Long> findIdsByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from MeetingRecord r
            where r.member.id = :memberId
            """)
    void deleteAllByMemberId(Long memberId);

    boolean existsByMemberIdAndMeetingId(Long memberId, Long meetingId);

    @Query("""
            select mr
            from MeetingRecord mr
            join fetch mr.meeting m
            where mr.member.id = :memberId
              and m.id = :meetingId
            """)
    Optional<MeetingRecord> findByMemberIdAndMeetingId(Long memberId, Long meetingId);
}
