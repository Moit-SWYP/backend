package pyws.swyp.notification.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.notification.dto.MemberToken;
import pyws.swyp.notification.entity.MemberDeviceToken;

public interface MemberDeviceTokenRepository extends JpaRepository<MemberDeviceToken, Long> {

    Optional<MemberDeviceToken> findByMemberIdAndToken(Long memberId, String token);

    boolean existsByMemberIdAndToken(Long memberId, String token);

    @Query("""
            select new pyws.swyp.notification.dto.MemberToken(mdt.memberId, mdt.token)
            from MemberDeviceToken mdt
            where mdt.memberId in :memberIds
            """)
    List<MemberToken> findTokensByMemberIds(List<Long> memberIds);
}
