package pyws.swyp.meeting.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.entity.RecordImage;

public interface RecordImageRepository extends JpaRepository<RecordImage, Integer> {

    @Query("""
        select r.imageKey
        from RecordImage r
        where r.meetingRecord.id = :recordId
        order by r.sortOrder asc
    """)
    List<String> findImageKeysByRecordId(Long recordId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           delete from RecordImage ri
           where ri.meetingRecord.id in :recordIds
           """)
    void deleteAllByRecordIds(List<Long> recordIds);

    List<RecordImage> findByMeetingRecordId(Long meetingRecordId);
}
