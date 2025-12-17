package pyws.swyp.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pyws.swyp.auth.service.JwtService;
import pyws.swyp.config.AuthPrincipalTestConfig;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.global.jwt.JwtProvider;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialAccountInfo;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Role;
import pyws.swyp.member.entity.SocialProvider;
import pyws.swyp.member.entity.WithdrawalType;
import pyws.swyp.member.service.MemberService;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthPrincipalTestConfig.class)
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MemberService memberService;

    @MockitoBean
    JwtProvider jwtProvider;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("로그인 정보 조회에 성공한다.")
    void getMember_success() throws Exception {
        // given
        Long memberId = 1L;

        MemberResponse response = new MemberResponse(
                "test@example.com",
                "테스트",
                java.time.LocalDate.of(1999, 1, 1),
                Gender.MALE,
                Role.MEMBER,
                List.of(new SocialAccountInfo(SocialProvider.KAKAO))
        );

        when(memberService.getMe(memberId)).thenReturn(response);

        // expected
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스트"))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.socialAccounts[0].socialProvider").value("KAKAO"));

        verify(memberService, times(1)).getMe(memberId);
    }


    @Test
    @DisplayName("로그인 정보 조회 시 존재하지 않는 회원이면 404와 에러코드를 반환한다")
    void getMember_memberNotFound() throws Exception {
        // given
        Long memberId = 1L;
        when(memberService.getMe(memberId))
                .thenThrow(ErrorCode.MEMBER_NOT_FOUND.toException());

        // expected
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.MEMBER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

        verify(memberService, times(1)).getMe(memberId);
    }

    @Test
    @DisplayName("회원 탈퇴에 성공한다.")
    void withdrawMember_success() throws Exception {
        // given
        Long memberId = 1L;

        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.BUG, null);

        doNothing().when(memberService).withdraw(eq(memberId), any(MemberWithdrawRequest.class));

        // expected
        mockMvc.perform(delete("/api/members/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(memberService, times(1)).withdraw(eq(memberId), any(MemberWithdrawRequest.class));
    }

    @Test
    @DisplayName("회원 탈퇴 사유가 기타일 때 사유 입력은 필수이다.")
    void withdrawMember_etcWithoutDescription_exception() throws Exception {
        // given
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.ETC, null);

        // expected
        mockMvc.perform(delete("/api/members/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.description").value("기타 사유를 입력해 주세요."));

        verify(memberService, never()).withdraw(anyLong(), any());
    }

    @Test
    @DisplayName("회원 탈퇴 시 description 500자 초과 시 400과 필드 에러를 반환한다")
    void withdrawMember_descriptionTooLong_400() throws Exception {
        // given
        String longDesc = "a".repeat(501);
        MemberWithdrawRequest request = new MemberWithdrawRequest(WithdrawalType.ETC, longDesc);

        // expected
        mockMvc.perform(delete("/api/members/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.description").value("기타 사유는 최대 500자까지 입력할 수 있습니다."));

        verify(memberService, never()).withdraw(anyLong(), any());
    }
}