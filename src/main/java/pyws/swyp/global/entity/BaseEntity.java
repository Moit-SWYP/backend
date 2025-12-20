package pyws.swyp.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Getter
@FilterDef(
        name = "activeFilter",
        defaultCondition = "is_active = true",
        autoEnabled = true
)
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = true)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isActive = true;

    public void delete() {
        isActive = false;
    }
}
