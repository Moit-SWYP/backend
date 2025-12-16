package pyws.swyp.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.entity.MeetingParticipant;
import pyws.swyp.meeting.entity.Role;

import java.util.Optional;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    @Query("""
        select role
        from MeetingParticipant
        where member.id = :memberId and meeting.id = :meetingId
    """)
    Optional<Role> findRoleByMeetingIdAndMemberId(Long memberId, Long meetingId);
}
