package pyws.swyp.member.repository.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.member.entity.friend.FriendGroup;

import java.util.List;

public interface FriendGroupRepository extends JpaRepository<FriendGroup, Long> {
    @Query("""
            SELECT fg
            FROM FriendGroup fg
            WHERE fg.owner.id = :memberId
    """)
    List<FriendGroup> findByMemberId(Long memberId);
}
