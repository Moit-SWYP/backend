package pyws.swyp.member.entity;

import jakarta.persistence.*;
import lombok.*;
import pyws.swyp.global.entity.BaseEntity;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialProvider socialProvider;

    @Column(nullable = false)
    private String social_id;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Gender gender;
}
