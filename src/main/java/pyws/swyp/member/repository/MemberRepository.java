package pyws.swyp.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
}
