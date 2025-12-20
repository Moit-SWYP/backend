package pyws.swyp.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.meeting.entity.MeetingParticipant;

import java.util.Optional;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    Optional<MeetingParticipant> findByMemberIdAndMeetingId(Long memberId, Long meetingId);

}
