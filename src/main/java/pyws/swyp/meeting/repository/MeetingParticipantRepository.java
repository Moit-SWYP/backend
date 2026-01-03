package pyws.swyp.meeting.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pyws.swyp.meeting.dto.MeetingBriefResponse;
import pyws.swyp.meeting.dto.ParticipantRow;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.MeetingStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    @Query("""
            SELECT mp
            FROM MeetingParticipant mp
            WHERE mp.member.id = :memberId
                AND mp.meeting.id = :meetingId
    """)
    Optional<MeetingParticipant> findByMemberIdAndMeetingId(Long memberId, Long meetingId);

    @Query("""
            SELECT new pyws.swyp.meeting.dto.MeetingBriefResponse(
                        mp.meeting.id,
                        mp.meeting.title,
                        mp.meeting.status,
                        mp.meeting.date
            )
            FROM MeetingParticipant mp
            WHERE mp.member.id = :memberId
            ORDER BY CASE WHEN mp.meeting.date IS NULL THEN 1 ELSE 0 END, mp.meeting.date ASC
    """)
    List<MeetingBriefResponse> findMeetingsByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT new pyws.swyp.meeting.dto.MeetingBriefResponse(
                        m.id,
                        m.title,
                        m.status,
                        m.date
            )
            FROM MeetingParticipant mp
            JOIN mp.meeting m
            WHERE mp.member.id = :memberId
                AND m.status IN :statuses
            ORDER BY CASE WHEN m.date IS NULL THEN 1 ELSE 0 END, m.date ASC
    """)
    List<MeetingBriefResponse> findMeetingsByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("statuses")Collection<MeetingStatus> statuses,
            Pageable pageable
    );

    @Query("""
            SELECT new pyws.swyp.meeting.dto.MeetingBriefResponse(
                        m.id,
                        m.title,
                        m.status,
                        m.date
            )
            FROM MeetingParticipant mp
            JOIN mp.meeting m
            WHERE mp.member.id = :memberId
                AND m.date BETWEEN :now AND :endDate
            ORDER BY m.date ASC
    """)
    List<MeetingBriefResponse> findMeetingsByMemberIdWithin7Days(
            @Param("memberId") Long memberId,
            @Param("now") LocalDate now,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT new pyws.swyp.meeting.dto.ParticipantRow(
                        mp.meeting.id,
                        new pyws.swyp.meeting.dto.ParticipantInfo(
                            mp.member.id,
                            mp.member.nickname,
                            mp.member.characterType,
                            mp.role
                        )
            )
            FROM MeetingParticipant mp
            WHERE mp.meeting.id IN :meetingIds
    """)
    List<ParticipantRow> findByMeetingIds(@Param("meetingIds") List<Long> meetingIds);

    @Query("""
            SELECT mp
            FROM MeetingParticipant mp
            WHERE mp.meeting.id = :meetingId
    """)
    List<MeetingParticipant> findByMeetingId(Long meetingId);

    @Query("""
            SELECT mp.member.id
            FROM MeetingParticipant mp
            WHERE mp.meeting.id = :meetingId
                AND mp.member.id <> :memberId
    """)
    List<Long> findMemberIdsByMeetingId(Long memberId, Long meetingId);

    boolean existsByMemberIdAndMeetingId(Long memberId, Long meetingId);

    @Query("""
            select (count(mp) > 0)
            from MeetingParticipant mp
            join mp.meeting m
            where mp.member.id = :memberId
              and mp.role = 'HOST'
              and m.status <> 'DONE'
              and m.isActive = true
            """)
    boolean existsHostInUncompletedMeetings(Long memberId);

    @Query("""
            select mp.id
            from MeetingParticipant mp
            where mp.member.id = :memberId
            """)
    List<Long> findIdsByMemberId(Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from MeetingParticipant mp
        where mp.id in :participantIds
    """)
    int deleteAllByIds(List<Long> participantIds);

    @Query("""
            select mp.member.id
            from MeetingParticipant mp
            where mp.meeting.id = :meetingId
            """)
    List<Long> findMemberIdsByMeetingId(Long meetingId);

    @Query("""
            select mp.member.id
            from MeetingParticipant mp
            where mp.meeting.id = :meetingId
              and not exists (
                    select 1
                    from DateVote dv
                    where dv.meetingParticipant = mp
              )
            """)
    List<Long> findMemberIdsNotVotedDate(Long meetingId);

    @Query("""
            select mp.member.id
            from MeetingParticipant mp
            where mp.meeting.id = :meetingId
              and not exists (
                    select 1
                    from TimeVote tv
                    where tv.meetingParticipant = mp
              )
            """)
    List<Long> findMemberIdsNotVotedTime(Long meetingId);
}
