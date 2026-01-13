package pyws.swyp.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    List<SocialAccount> findByMember(Member member);

    Optional<SocialAccount> findByMemberIdAndSocialProvider(Long memberId, SocialProvider socialProvider);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SocialAccount sa where sa.member.id = :memberId")
    int deleteAllByMemberId(Long memberId);

    long countByMemberId(Long memberId);
}
