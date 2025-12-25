package pyws.swyp.meeting.repository.vote;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.entity.vote.DateOption;

public interface DateOptionRepository extends JpaRepository<DateOption, Long> {

    List<DateOption> findAllByMeetingIdAndDateIn(Long meetingId, List<LocalDate> dates);

    @Query("""
            select do.date
            from DateOption do
            join DateVote dv on dv.dateOption = do
            where do.meeting.id = :meetingId
            group by do.id, do.date
            order by count(dv.id) desc, do.date asc
            """)
    List<LocalDate> findRankedDatesByMeetingId(Long meetingId, Pageable pageable);

    @Query("""
            select distinct do.date
            from DateVote dv
            join dv.dateOption do
            where do.meeting.id = :meetingId
            order by do.date asc
            """)
    List<LocalDate> findVotedDatesByMeetingId(Long meetingId);
}
