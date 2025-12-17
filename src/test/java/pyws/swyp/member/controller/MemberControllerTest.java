package pyws.swyp.member.controller;

import static java.time.LocalDate.of;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pyws.swyp.config.AuthPrincipalTestConfig;
import pyws.swyp.config.AuthTestPrincipalContext;
import pyws.swyp.config.TestRedisConfig;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialLinkRequest;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.Role;
import pyws.swyp.member.entity.SocialAccount;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.entity.WithdrawalType;
import pyws.swyp.member.repository.MemberRepository;
import pyws.swyp.member.repository.MemberWithdrawalRepository;
import pyws.swyp.member.repository.SocialAccountRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import({AuthPrincipalTestConfig.class, TestRedisConfig.class})
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SocialAccountRepository socialAccountRepository;

    @Autowired
    MemberWithdrawalRepository memberWithdrawalRepository;

    private Long memberId;
    private SocialProvider provider;

    @BeforeEach
    void setUp() {
        socialAccountRepository.deleteAll();
        memberWithdrawalRepository.deleteAll();
        memberRepository.deleteAll();

        Member member = memberRepository.save(Member.builder()
                .email("test@example.com")
                .nickname("테스트")
                .gender(Gender.MALE)
                .birthDate(of(1999, 1, 1))
                .role(Role.MEMBER)
                .build());

        SocialProvider kakao = SocialProvider.KAKAO;
        socialAccountRepository.save(SocialAccount.builder()
                .member(member)
                .socialProvider(kakao)
                .socialId("social-id-123")
                .build());

        this.memberId = member.getId();
        this.provider = kakao;
        AuthTestPrincipalContext.setMemberId(member.getId());
    }

    @Test
    @DisplayName("로그인 정보 조회에 성공한다.")
    void getMember_success() throws Exception {
        // expected
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스트"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.socialAccounts[0].socialProvider").value(this.provider.name()));
    }

    @Test
    @DisplayName("로그인 정보 조회 시 존재하지 않는 회원이면 404와 에러코드를 반환한다")
    void getMember_memberNotFound() throws Exception {
        // given
        socialAccountRepository.deleteAll();
        memberRepository.deleteAll();

        // expected
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.MEMBER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴에 성공한다.")
    void withdrawMember_success() throws Exception {
        // given
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.BUG, null);

        // expected
        mockMvc.perform(delete("/api/members/withdraw").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());

        // then (예시: 탈퇴 테이블이 저장되는 설계라면)
        assertTrue(memberWithdrawalRepository.count() >= 1);
    }

    @Test
    @DisplayName("회원 탈퇴 사유가 기타일 때 사유 입력은 필수이다.")
    void withdrawMember_etcWithoutDescription_exception() throws Exception {
        // given
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.ETC, null);

        // expected
        mockMvc.perform(delete("/api/members/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.description").value("기타 사유를 입력해 주세요."));
    }

    @Test
    @DisplayName("회원 탈퇴 시 description 500자 초과 시 400과 필드 에러를 반환한다")
    void withdrawMember_descriptionTooLong_400() throws Exception {
        // given
        String longDesc = "a".repeat(501);
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.ETC, longDesc);

        // expected
        mockMvc.perform(delete("/api/members/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.description").value("기타 사유는 최대 500자까지 입력할 수 있습니다."));
    }

    @Test
    @DisplayName("소셜 계정 연동에 성공한다")
    void linkSocialAccount_success() throws Exception {
        // given
        SocialProvider naver = SocialProvider.NAVER;
        SocialLinkRequest request = new SocialLinkRequest(naver, "social-id-456");

        // when
        mockMvc.perform(post("/api/members/me/social-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        assertTrue(socialAccountRepository.findByMemberIdAndSocialProvider(this.memberId, naver).isPresent());
    }

    @Test
    @DisplayName("같은 소셜 계정은 하나만 연동이 가능하다.")
    void linkSocialAccount_conflict_409() throws Exception {
        // given
        SocialLinkRequest request = new SocialLinkRequest(this.provider, "social-id-123");

        // expected
        mockMvc.perform(post("/api/members/me/social-accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("소셜 계정 연동 시 요청 바디가 유효하지 않으면 400과 에러코드를 반환한다")
    void linkSocialAccount_invalidRequest_400() throws Exception {
        // given
        SocialLinkRequest request = new SocialLinkRequest(SocialProvider.NAVER, null);

        // expected
        mockMvc.perform(post("/api/members/me/social-accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.socialId").value("소셜 ID는 필수입니다."));
    }

    @Test
    @DisplayName("소셜 계정 연동 해제에 성공한다")
    void unlinkSocialAccount_success() throws Exception {
        // when
        mockMvc.perform(delete("/api/members/me/social-accounts/" + this.provider.name()))
                .andExpect(status().isOk());

        // then
        assertTrue(socialAccountRepository.findByMemberIdAndSocialProvider(this.memberId, this.provider).isEmpty());
    }

    @Test
    @DisplayName("소셜 계정 연동 해제 시 provider가 enum에 없으면 400을 반환한다")
    void unlinkSocialAccount_invalidProvider_400() throws Exception {
        // expected
        String invalidProvider = "NOT_A_PROVIDER";
        mockMvc.perform(delete("/api/members/me/social-accounts/" + invalidProvider))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.provider")
                        .value("값의 형식이 올바르지 않습니다. (입력값: " + invalidProvider + ")"));
    }
}
