package pyws.swyp.meeting.repository.vote;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pyws.swyp.meeting.entity.vote.TimeVote;

public interface TimeVoteRepository extends JpaRepository<TimeVote, Long> {
    List<TimeVote> findAllByMeetingParticipantId(Long participantId);

    @Query("""
            select tv.timeOption.id
            from TimeVote tv
            where tv.timeOption.id in :optionIds
            group by tv.timeOption.id
            """)
    List<Long> findExistingOptionIds(List<Long> optionIds);

    @Query("""
            select tv
            from TimeVote tv
            join tv.timeOption to
            where to.meeting.id = :meetingId
              and to.time = :time
            """)
    List<TimeVote> findAllByMeetingIdAndTime(Long meetingId, LocalTime time);

    @Query("""
            select tv.timeOption.id
            from TimeVote tv
            where tv.meetingParticipant.id = :meetingParticipantId
            """)
    List<Long> findOptionIdsByMeetingParticipantId(Long meetingParticipantId);
}
