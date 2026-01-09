package pyws.swyp.meeting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pyws.swyp.global.entity.BaseEntity;
import pyws.swyp.member.entity.Member;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"meetind_id", "member_id"}
                )
        }
)
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
    private ParticipantRole role;

    @Builder
    public MeetingParticipant(Meeting meeting, Member member, ParticipantRole role) {
        this.meeting = meeting;
        this.member = member;
        this.role = role;
    }

    public static MeetingParticipant host(Meeting meeting, Member member) {
        return MeetingParticipant.builder()
                .meeting(meeting)
                .member(member)
                .role(ParticipantRole.HOST)
                .build();
    }

    public static MeetingParticipant member(Meeting meeting, Member member) {
        return MeetingParticipant.builder()
                .meeting(meeting)
                .member(member)
                .role(ParticipantRole.MEMBER)
                .build();
    }

    public boolean isHost() {
        return ParticipantRole.HOST.equals(this.role);
    }
}
