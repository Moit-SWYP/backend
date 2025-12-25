package pyws.swyp.meeting.repository.vote;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pyws.swyp.meeting.dto.vote.VotedTimeResponse;
import pyws.swyp.meeting.entity.vote.TimeOption;

public interface TimeOptionRepository extends JpaRepository<TimeOption, Long> {

    List<TimeOption> findAllByMeetingIdAndTimeIn(Long meetingId, List<LocalTime> times);

    @Query(
            value = """
                        select topt.time
                        from time_vote tv
                        join time_option topt on tv.time_option_id = topt.id
                        where topt.meeting_id = :meetingId
                        group by topt.time
                        order by count(tv.id) desc, topt.time asc
                        limit 1
                    """,
            nativeQuery = true
    )
    Optional<LocalTime> findTopRankedTimeByMeetingId(@Param("meetingId") Long meetingId);

    @Query("""
            select new pyws.swyp.meeting.dto.vote.VotedTimeResponse(to.time, count(tv.id))
            from TimeVote tv
            join tv.timeOption to
            where to.meeting.id = :meetingId
            group by to.time
            order by to.time asc
            """)
    List<VotedTimeResponse> findVotedTimesWithCounts(@Param("meetingId") Long meetingId);
}
