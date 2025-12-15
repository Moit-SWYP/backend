package pyws.swyp.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.meeting.entity.Meeting;

public interface MeetingRepository extends JpaRepository<Meeting,Long> {
}
