package pyws.swyp.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.member.dto.SocialLinkRequest;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class SocialAccountService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    public void link(Long memberId, SocialLinkRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(ErrorCode.MEMBER_NOT_FOUND::toException);

        try {
            SocialAccount socialAccount = SocialAccount.builder()
                    .member(member)
                    .socialProvider(request.socialProvider())
                    .socialId(request.socialId())
                    .build();

            socialAccountRepository.save(socialAccount);
        } catch (DataIntegrityViolationException e) {
            throw ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.toException();
        }
    }

    public void unlink(Long memberId, SocialProvider socialProvider) {
        SocialAccount socialAccount = socialAccountRepository
                .findByMemberIdAndSocialProvider(memberId, socialProvider)
                        .orElseThrow(ErrorCode.SOCIAL_ACCOUNT_FOUND::toException);

        socialAccountRepository.delete(socialAccount);
    }
}

