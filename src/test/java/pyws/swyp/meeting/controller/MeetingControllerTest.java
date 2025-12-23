package pyws.swyp.meeting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pyws.swyp.config.AuthPrincipalTestConfig;
import pyws.swyp.config.AuthTestPrincipalContext;
import pyws.swyp.global.jwt.JwtProvider;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.dto.MeetingUpdateRequest;
import pyws.swyp.meeting.service.MeetingService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import({AuthPrincipalTestConfig.class})
@RequiredArgsConstructor
public class MeetingControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    MeetingService meetingService;

    @MockitoBean
    JwtProvider jwtProvider;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("정상 요청 시 모임 생성 성공")
    void 모임_생성_성공() throws Exception{
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        MeetingCreateRequest request = new MeetingCreateRequest(
                "모잇 오프라인",
                LocalDate.of(2025,12,30),
                LocalDateTime.of(2025,12,15,23,59),
                LocalDateTime.of(2025,12,26,23,59)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(meetingService, times(1)).createMeeting(memberId, request);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("모임 이름 Blank일 시 생성 실패")
    void 모임_생성_실패_모임_이름_blank() throws Exception{
        // given
        MeetingCreateRequest request = new MeetingCreateRequest(
                "  ",
                LocalDate.of(2025,12,30),
                LocalDateTime.of(2025,12,15,23,59),
                LocalDateTime.of(2025,12,26,23,59)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/meetings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verifyNoInteractions(meetingService);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("정상 요청 시 모임 삭제 성공")
    void 모임_삭제_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        Long meetingId = 1L;

        // when
        MvcResult mvcResult = mockMvc.perform(delete("/api/meetings/{id}", meetingId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(meetingService, times(1)).deleteMeeting(memberId, meetingId);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("잘못된 형식의 meetingId 넘겼을 때 모임 삭제 실패")
    void 모임_삭제_실패_잘못된_pathVariable() throws Exception {
        // given
        String meetingId = "abc";

        // when
        MvcResult mvcResult = mockMvc.perform(delete("/api/meetings/{id}", meetingId))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verifyNoInteractions(meetingService);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("정상 요청 시 모임 탈퇴 성공")
    void 모임_탈퇴_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        Long meetingId = 1L;

        // when
        MvcResult mvcResult = mockMvc.perform(delete("/api/meetings/quit/{id}", meetingId))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(meetingService, times(1)).quitMeeting(memberId, meetingId);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("잘못된 형식의 meetingId 넘겼을 때 모임 삭제 실패")
    void 모임_탈퇴_실패_잘못된_pathVariable() throws Exception {
        // given
        String meetingId = "abc";

        // when
        MvcResult mvcResult = mockMvc.perform(delete("/api/meetings/quit/{id}", meetingId))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verifyNoInteractions(meetingService);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("정상 요청 시 모임 수정 성공")
    void 모임_수정_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        Long meetingId = 1L;

        MeetingUpdateRequest request = new MeetingUpdateRequest(
                "모잇 오프라인",
                LocalDate.of(2025,12,30),
                LocalDateTime.of(2025,12,15,23,59),
                LocalDateTime.of(2025,12,26,23,59)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/meetings/{id}", meetingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(meetingService, times(1)).updateMeeting(memberId, meetingId, request);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());

    }

    @Test
    @DisplayName("잘못된 형식의 meetingId 넘겼을 때 모임 삭제 실패")
    void 모임_수정_실패() throws Exception {
        // given
        String meetingId = "abc";

        MeetingUpdateRequest request = new MeetingUpdateRequest(
                "모잇 오프라인",
                LocalDate.of(2025,12,30),
                LocalDateTime.of(2025,12,15,23,59),
                LocalDateTime.of(2025,12,26,23,59)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(patch("/api/meetings/{id}", meetingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verifyNoInteractions(meetingService);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("정상 요청 시 전체 모임 조회 성공")
    void 전체_모임_조회_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/meetings/all"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(meetingService, times(1)).getAllMeetings(memberId);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("정상 요청 시 기다리고 있는 모임 조회 성공")
    void 기다리고_있는_모임_조회_성공() throws Exception {
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/meetings/waiting"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(meetingService, times(1)).getWaitingMeetings(eq(memberId), any(Pageable.class));
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }
}
