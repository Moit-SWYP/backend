package pyws.swyp.meeting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.dto.ReviewSummary;
import pyws.swyp.meeting.entity.MeetingReview;

public interface MeetingReviewRepository extends JpaRepository<MeetingReview, Integer> {


    boolean existsByMeetingParticipantId(Long participantId);

    Optional<MeetingReview> findByMeetingParticipantId(Long participantId);

    @Query("""
            select new pyws.swyp.meeting.dto.ReviewSummary(
                m.id,
                case when r.id is not null then true else false end,
                (
                    select ri.imageKey
                    from ReviewImage ri
                    where ri.meetingReview = r
                      and ri.sortOrder = (
                          select min(ri2.sortOrder)
                          from ReviewImage ri2
                          where ri2.meetingReview = r
                      )
                ),
                r.content
            )
            from Meeting m
            left join MeetingParticipant mp
                   on mp.meeting = m
                  and mp.member.id = :memberId
            left join MeetingReview r
                   on r.meetingParticipant = mp
            where m.id in :meetingIds
            """)
    List<ReviewSummary> findMyReviewSummaryByMeetingIds(List<Long> meetingIds, Long memberId);

    @Query("""
           select r.id
           from MeetingReview r
           where r.meetingParticipant.id in :participantIds
           """)
    List<Long> findIdsByParticipantIds(List<Long> participantIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           delete from MeetingReview r
           where r.meetingParticipant.id in :participantIds
           """)
    void deleteAllByParticipantIds(List<Long> participantIds);
}
