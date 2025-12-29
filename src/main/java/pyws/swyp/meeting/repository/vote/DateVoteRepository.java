package pyws.swyp.meeting.repository.vote;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.dto.vote.VoterResponse;
import pyws.swyp.meeting.entity.vote.DateVote;

public interface DateVoteRepository extends JpaRepository<DateVote, Long> {

    List<DateVote> findAllByMeetingParticipantId(Long meetingParticipantId);

    void deleteAllByMeetingParticipantId(Long participantId);

    @Query("""
            select dv.date
            from DateVote dv
            where dv.meeting.id = :meetingId
            group by dv.date
            having count (dv.id) = (
                select max(cnt)
                from (
                   select count(dv.id) as cnt
                   from DateVote dv
                   where dv.meeting.id = :meetingId
                   group by dv.date
                )
            )
            order by dv.date asc
            """)
    List<LocalDate> findTopDatesByMeetingId(Long meetingId, Pageable pageable);

    @Query("""
            select distinct dv.date
            from DateVote dv
            where dv.meeting.id = :meetingId
            order by dv.date asc
            """)
    List<LocalDate> findVotedDatesByMeetingId(Long meetingId);

    @Query("""
            select new pyws.swyp.meeting.dto.vote.VoterResponse(
                m.id,
                m.nickname,
                m.characterType
            )
            from DateVote dv
            join dv.meetingParticipant mp
            join mp.member m
            where dv.meeting.id = :meetingId
              and dv.date = :date
            order by m.id asc
            """)
    List<VoterResponse> findVotersByMeetingIdAndDate(Long meetingId, LocalDate date);
}

