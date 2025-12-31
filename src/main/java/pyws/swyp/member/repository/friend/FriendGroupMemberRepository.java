package pyws.swyp.member.repository.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.member.entity.friend.FriendGroupMember;

import java.util.List;

public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMember, Long> {

    @Query("""
            SELECT fgm
            FROM FriendGroupMember fgm
            JOIN FETCH fgm.friendGroup
            JOIN FETCH fgm.member
            WHERE fgm.friendGroup.id IN :friendGroupIds
    """)
    List<FriendGroupMember> findByFriendGroupIds(List<Long> friendGroupIds);
}
