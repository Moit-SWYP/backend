package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pyws.swyp.config.AuthPrincipalTestConfig;
import pyws.swyp.config.AuthTestPrincipalContext;
import pyws.swyp.global.jwt.JwtProvider;
import pyws.swyp.meeting.service.InvitationService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import({AuthPrincipalTestConfig.class})
@RequiredArgsConstructor
public class InvitationControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    InvitationService invitationService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    @DisplayName("초대를 위한 토큰 반환 성공")
    void 초대_토큰_조회_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        Long meetingId = 1L;

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/meetings/{meetingId}/invitations/link", meetingId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(invitationService, times(1)).createInvitationLink(eq(memberId), eq(meetingId));
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("잘못된 형식의 meetingId 넘겼을 때 초대 토큰 반환 실패")
    void 초대_토큰_조회_실패_잘못된_pathVariable() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        String meetingId = "abc";

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/meetings/{meetingId}/invitations/link", meetingId))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verifyNoInteractions(invitationService);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("토큰을 통해 모임 조인 성공")
    void 모임_조인_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        String inviteToken = UUID.randomUUID().toString();

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/meetings/invitations/join")
                        .param("inviteToken", inviteToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(invitationService, times(1)).joinMeetingFromLink(eq(memberId), eq(inviteToken));
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }
}
