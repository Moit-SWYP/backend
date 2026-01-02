package pyws.swyp.member.entity.friend;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Member friend;

    @Column(nullable = false)
    private Integer metCount;

    @Builder
    public Friendship(Member member, Member friend) {
        this.member = member;
        this.friend = friend;
        this.metCount = 1;
    }

    public void increaseMetCount() {
        this.metCount += 1;
    }
}
