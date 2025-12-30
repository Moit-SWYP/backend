package pyws.swyp.meeting.repository.vote;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.dto.vote.time.VotedTimeResponse;
import pyws.swyp.meeting.dto.vote.VoterResponse;
import pyws.swyp.meeting.entity.vote.TimeVote;

public interface TimeVoteRepository extends JpaRepository<TimeVote, Long> {

    List<TimeVote> findAllByMeetingParticipantId(Long participantId);

    void deleteAllByMeetingParticipantId(Long id);

    @Query("""
            select tv.time
            from TimeVote tv
            where tv.meeting.id = :meetingId
            group by tv.time
            having count(tv.id) = (
                select max(cnt)
                from (
                    select count(tv2.id) as cnt
                    from TimeVote tv2
                    where tv2.meeting.id = :meetingId
                    group by tv2.time
                )
            )
            order by tv.time asc
            """)
    List<LocalTime> findTopTimesByMeetingId(Long meetingId, Pageable pageable);

    @Query("""
            select new pyws.swyp.meeting.dto.vote.time.VotedTimeResponse(
                tv.time,
                count(tv.id)
            )
            from TimeVote tv
            where tv.meeting.id = :meetingId
            group by tv.time
            order by count(tv.id) desc, tv.time asc
            """)
    List<VotedTimeResponse> findVotedTimesWithCounts(Long meetingId);

    @Query("""
            select new pyws.swyp.meeting.dto.vote.VoterResponse(
                m.id,
                m.nickname,
                m.characterType
            )
            from TimeVote tv
            join tv.meetingParticipant mp
            join mp.member m
            where tv.meeting.id = :meetingId
              and tv.time = :time
            order by m.id asc
            """)
    List<VoterResponse> findVotersByMeetingIdAndTime(Long meetingId, LocalTime time);

    @Query("""
            select distinct tv.time
            from TimeVote tv
            where tv.meeting.id = :meetingId
            order by tv.time asc
            """)
    List<LocalTime> findVotedTimesByMeetingId(Long meetingId);
}
