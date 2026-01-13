package pyws.swyp.member.repository.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.member.entity.friend.Friendship;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE f.member.id = :memberId
                AND f.friend.id IN :friendIds
    """)
    List<Friendship> findByMemberIdAndFriendIds(Long memberId, List<Long> friendIds);

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE f.member.id = :memberId
    """)
    List<Friendship> findByMemberId(Long memberId);

    @Query("""
            SELECT f
            FROM Friendship f
            WHERE f.member.id IN :memberIds
        """)
    List<Friendship> findByMemberIds(List<Long> memberIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM Friendship f
            WHERE f.member.id = :memberId
                OR f.friend.id = :memberId
    """)
    void deleteAllByMemberIds(Long memberId);
}
