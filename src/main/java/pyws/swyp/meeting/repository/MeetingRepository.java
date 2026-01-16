package pyws.swyp.meeting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Meeting> findByDateBetweenOrderByDateAscTimeAsc(LocalDate start, LocalDate end);

    @Query("""
            select m.id
            from Meeting m
            where m.status = 'DONE'
              and m.date = :yesterday
            """)
    List<Long> findIdsForReviewReminder(LocalDate yesterday);
}
