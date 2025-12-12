package pyws.swyp.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pyws.swyp.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SocialAccount extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialProvider socialProvider;

    @Column(nullable = false)
    private String socialId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
}
