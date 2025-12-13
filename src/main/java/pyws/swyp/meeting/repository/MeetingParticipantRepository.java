package pyws.swyp.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.meeting.entity.MeetingParticipant;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
}
