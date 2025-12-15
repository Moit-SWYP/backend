package pyws.swyp.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findBySocialProviderAndSocialId(SocialProvider provider, String socialId);
}
