package pyws.swyp.meeting.entity;

import jakarta.persistence.*;
import lombok.*;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Builder
    public MeetingParticipant(Meeting meeting, Member member, String role) {
        this.meeting = meeting;
        this.member = member;
        this.role = Role.valueOf(role);
    }
}
