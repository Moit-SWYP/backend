package pyws.swyp.meeting.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.meeting.entity.ReviewImage;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Integer> {

    @Query("""
        select r.imageKey
        from ReviewImage r
        where r.meetingReview.id = :reviewId
        order by r.sortOrder asc
    """)
    List<String> findImageKeysByReviewId(Long reviewId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           delete from ReviewImage ri
           where ri.meetingReview.id in :reviewIds
           """)
    void deleteAllByReviewIds(List<Long> reviewIds);
}
