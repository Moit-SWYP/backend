package pyws.swyp.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.member.controller.api.MemberApi;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.service.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @GetMapping("/me")
    public MemberResponse getMember(@AuthenticationPrincipal Long memberId) {
        return memberService.getMe(memberId);
    }

    @DeleteMapping("/withdraw")
    public void withdrawMember(@AuthenticationPrincipal Long memberId,
                               @RequestBody @Validated MemberWithdrawRequest request) {
        memberService.withdraw(memberId, request);
    }
}
