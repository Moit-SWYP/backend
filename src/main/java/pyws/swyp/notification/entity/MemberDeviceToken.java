package pyws.swyp.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import pyws.swyp.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Filter(name = "activeFilter")
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_device_token",
                columnNames = {"memberId", "token"}
        )
)
public class MemberDeviceToken extends BaseEntity {

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String token;

    @Builder
    public MemberDeviceToken(Long memberId, String token) {
        this.memberId = memberId;
        this.token = token;
    }
}
