package pyws.swyp.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.member.controller.api.MemberApi;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialLinkRequest;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.service.MemberService;
import pyws.swyp.member.service.SocialAccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;
    private final SocialAccountService socialAccountService;

    @GetMapping("/me")
    public MemberResponse getMember(@AuthenticationPrincipal Long memberId) {
        return memberService.getMe(memberId);
    }

    @DeleteMapping("/withdraw")
    public void withdrawMember(@AuthenticationPrincipal Long memberId,
                               @RequestBody @Validated MemberWithdrawRequest request) {
        memberService.withdraw(memberId, request);
    }

    @PostMapping("/me/social-accounts")
    public void link(@AuthenticationPrincipal Long memberId,
                     @RequestBody @Validated SocialLinkRequest request) {
        socialAccountService.link(memberId, request);
    }

    @DeleteMapping("/me/social-accounts/{provider}")
    public void unlink(@AuthenticationPrincipal Long memberId,
                       @PathVariable SocialProvider provider) {
        socialAccountService.unlink(memberId, provider);
    }
}
