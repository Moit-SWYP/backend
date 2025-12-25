package pyws.swyp.meeting.repository.vote;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pyws.swyp.meeting.entity.vote.DateVote;

public interface DateVoteRepository extends JpaRepository<DateVote, Long> {

    @Query("""
            select dv.dateOption.id
            from DateVote dv
            where dv.dateOption.id in :optionIds
            group by dv.dateOption.id
            """)
    List<Long> findExistingOptionIds(@Param("optionIds") List<Long> optionIds);

    @Query("""
            select dv.dateOption.id
            from DateVote dv
            where dv.meetingParticipant.id = :meetingParticipantId
            """)
    List<Long> findOptionIdsByMeetingParticipantId(@Param("meetingParticipantId") Long meetingParticipantId);

    List<DateVote> findAllByMeetingParticipantId(Long meetingParticipantId);

    @Query("""
            select dv
            from DateVote dv
                join fetch dv.dateOption do
                join fetch dv.meetingParticipant mp
                join fetch mp.member m
            where do.meeting.id = :meetingId
              and do.date between :startDate and :endDate
            """)
    List<DateVote> findAllByMeeting(
            @Param("meetingId") Long meetingId);

    @Query("""
            select dv
            from DateVote dv
                join fetch dv.meetingParticipant mp
                join fetch mp.member m
                join dv.dateOption o
            where o.meeting.id = :meetingId
              and o.date = :date
            """)
    List<DateVote> findAllByMeetingIdAndDate(Long meetingId, LocalDate date);
}

