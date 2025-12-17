package pyws.swyp.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);

    List<SocialAccount> findByMember(Member member);

    Optional<SocialAccount> findByMemberIdAndSocialProvider(Long memberId, SocialProvider socialProvider);
}
