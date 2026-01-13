package pyws.swyp.meeting.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import pyws.swyp.config.AuthPrincipalTestConfig;
import pyws.swyp.config.AuthTestPrincipalContext;
import pyws.swyp.config.TestRedisConfig;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.meeting.entity.*;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.Gender;
import pyws.swyp.member.entity.Member;
import pyws.swyp.member.entity.MemberRole;
import pyws.swyp.member.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import({AuthPrincipalTestConfig.class, TestRedisConfig.class})
class MeetingConfirmControllerTest {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    MeetingParticipantRepository meetingParticipantRepository;
    @Autowired
    DateVoteRepository dateVoteRepository;
    @Autowired
    TimeVoteRepository timeVoteRepository;

    private Long meetingId;
    private Long memberId;

    @BeforeEach
    void setUp() {
        dateVoteRepository.deleteAll();
        timeVoteRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        Member member = memberRepository.save(Member.builder()
                .email("host@test.com")
                .nickname("host")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1999, 1, 1))
                .role(MemberRole.MEMBER)
                .characterType(CharacterType.ACTIVE)
                .build());
        this.memberId = member.getId();

        Meeting meeting = meetingRepository.save(Meeting.builder()
                .title("확정 테스트 모임")
                .type(MeetingType.DRINKER)
                .build());
        this.meetingId = meeting.getId();

        meetingParticipantRepository.save(MeetingParticipant.builder()
                .meeting(meeting)
                .member(member)
                .role(ParticipantRole.HOST)
                .build());

        AuthTestPrincipalContext.setMemberId(this.memberId);
    }

    @Test
    @DisplayName("모임장이 지정한 날짜로 투표를 확정한다.")
    void confirmDateManual_success() throws Exception {
        // given
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        meeting.updateStatus(MeetingStatus.DATE_VOTING);
        meetingRepository.save(meeting);

        LocalDate chosen = LocalDate.of(2025, 12, 20);

        // when
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/date/confirm/manual", meetingId)
                        .param("date", chosen.toString()))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        Meeting updated = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.DATE_VOTED, updated.getStatus());
        assertEquals(chosen, updated.getDate());
    }

    @Test
    @DisplayName("모임장이 지정한 시간으로 투표를 확정한다.")
    void confirmTimeManual_success() throws Exception {
        // given
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        meeting.updateStatus(MeetingStatus.TIME_VOTING);
        meetingRepository.save(meeting);

        LocalTime chosen = LocalTime.of(15, 0);

        // when
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/time/confirm/manual", meetingId)
                        .param("time", chosen.toString()))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        Meeting updated = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.TIME_VOTED, updated.getStatus());
        assertEquals(chosen, updated.getTime());
    }

    @Test
    @DisplayName("날짜 확정을 취소한다.")
    void cancelConfirmDate_success() throws Exception {
        // given
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        meeting.updateStatus(MeetingStatus.DATE_VOTING);
        meetingRepository.save(meeting);

        LocalDate date = LocalDate.of(2025, 12, 20);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/date/confirm/manual", meetingId)
                        .param("date", date.toString()))
                .andExpect(status().isOk());

        // when
        mockMvc.perform(delete("/api/meetings/{meetingId}/votes/date/confirm", meetingId))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        Meeting updated = meetingRepository.findById(meetingId).orElseThrow();
        assertEquals(MeetingStatus.DATE_VOTING, updated.getStatus());
        assertNull(updated.getDate());
    }

    @Test
    @DisplayName("시간 형식이 올바르지 않으면 400과 필드 에러를 반환한다.")
    void confirmTimeManual_invalidTimeFormat_400() throws Exception {
        // given
        String invalidTime = "99:99";

        // expected
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/time/confirm/manual", meetingId)
                        .param("time", invalidTime))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.time")
                        .value("값의 형식이 올바르지 않습니다. (입력값: " + invalidTime + ")"));
    }
}