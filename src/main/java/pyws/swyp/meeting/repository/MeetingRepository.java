package pyws.swyp.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.meeting.entity.Meeting;

import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting,Long> {
    Optional<Meeting> findByPublicId(UUID publicId);
}
