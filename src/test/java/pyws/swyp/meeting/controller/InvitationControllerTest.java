package pyws.swyp.meeting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pyws.swyp.config.AuthPrincipalTestConfig;
import pyws.swyp.config.AuthTestPrincipalContext;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.global.jwt.JwtProvider;
import pyws.swyp.meeting.dto.InviteFriendsRequest;
import pyws.swyp.meeting.service.InvitationService;

import java.util.List;
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

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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

    @Test
    @DisplayName("친구 리스트로 모임에 초대 성공")
    void 친구_목록_초대_성공() throws Exception {
        // given
        Long memberId = 1L;
        Long meetingId = 1L;

        InviteFriendsRequest request = new InviteFriendsRequest(List.of(2L, 3L));

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/meetings/{meetingId}/invitations", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(invitationService).inviteToMeeting(eq(memberId), eq(meetingId), any());
    }

    // Todo: 현재는 실제 DB 연결 x, 단순 서비스 단의 exception을 어떻게 처리하는지 테스트
    //  추후 테스트 코드 리팩토링 시 고려할 것.
//    @Test
//    @DisplayName("존재하지 않는 멤버 초대 시 400 응답")
//    void 친구_목록_초대_실패_존재하지_않는_멤버_초대() throws Exception {
//        // given
//        Long memberId = 1L;
//        Long meetingId = 1L;
//
//        InviteFriendsRequest request = new InviteFriendsRequest(List.of(2L));
//
//        doThrow(ErrorCode.INVALID_INVITE_MEMBER.toException())
//                .when(invitationService)
//                .inviteToMeeting(eq(memberId), eq(meetingId), any());
//
//        // when & then
//        mockMvc.perform(post("/api/meetings/{meetingId}/invitations", meetingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request))
//                ).andExpect(status().isBadRequest());
//    }
}
