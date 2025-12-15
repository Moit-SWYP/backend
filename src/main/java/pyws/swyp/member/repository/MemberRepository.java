package pyws.swyp.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.meeting.entity.Meeting;

public interface MemberRepository extends JpaRepository<Meeting,Long> {

}
