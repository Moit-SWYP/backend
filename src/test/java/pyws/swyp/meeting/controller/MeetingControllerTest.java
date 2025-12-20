package pyws.swyp.meeting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pyws.swyp.meeting.dto.MeetingCreateRequest;
import pyws.swyp.meeting.service.MeetingService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeetingController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
public class MeetingControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    MeetingService meetingService;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // Todo: jwtFilter 관련 테스트 문제 해결 후 수정 예정.

//    @Test
//    @DisplayName("정상 요청 시 모임 생성 성공")
//    void 모임_생성_성공() throws Exception{
//        // given
//        Long memberId = 1L;
//
//        MeetingCreateRequest request = new MeetingCreateRequest(
//                "모잇 오프라인",
//                LocalDateTime.of(2025,12,30,14,0),
//                LocalDateTime.of(2025,12,15,23,59),
//                LocalDateTime.of(2025,12,26,23,59)
//        );
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(post("/api/meetings")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                )
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andReturn();
//
//        // then
//        verify(meetingService, times(1)).createMeeting(memberId, request);
//        Integer status = mvcResult.getResponse().getStatus();
//        assertThat(status).isEqualTo(HttpStatus.OK.value());
//    }
//
//    @Test
//    @DisplayName("모임 이름 Blank일 시 생성 실패")
//    void 모임_생성_실패_모임_이름_blank() throws Exception{
//        // given
//        MeetingCreateRequest request = new MeetingCreateRequest(
//                "  ",
//                LocalDateTime.of(2025,12,30,14,0),
//                LocalDateTime.of(2025,12,15,23,59),
//                LocalDateTime.of(2025,12,26,23,59)
//        );
//
//        // when
//        MvcResult mvcResult = mockMvc.perform(post("/api/meetings")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(request))
//                )
//                .andExpect(status().isBadRequest())
//                .andDo(print())
//                .andReturn();
//
//        // then
//        verifyNoInteractions(meetingService);
//        Integer status = mvcResult.getResponse().getStatus();
//        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
//    }

    // Todo: admin 관련 테스트 코드 작성 후 추가 예정.
    void 모임_삭제_성공() throws Exception {

    }

    void 모임_삭제_실패() throws Exception {

    }

    void 모임_탈퇴_성공() throws Exception {

    }

    void 모임_탈퇴_실패() throws Exception {

    }

    void 모임_수정_성공() throws Exception {

    }

    void 모임_수정_실패() throws Exception {

    }
}
