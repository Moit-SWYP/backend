package pyws.swyp.meeting.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import pyws.swyp.meeting.entity.Meeting;
import pyws.swyp.meeting.entity.MeetingStatus;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Meeting m
            set m.status = :doneStatus
            where m.status <> :doneStatus
              and m.date < :today
            """)
    int bulkMarkMeetingsDone(
            @Param("today") LocalDate today,
            @Param("doneStatus") MeetingStatus doneStatus
    );
           
    @Query("""
            select m.id
            from Meeting m
            where m.status = :meetingStatus
            """)
    List<Long> findIdsByStatus(MeetingStatus meetingStatus);

    Optional<Meeting> findByPublicId(UUID publicId);
}
