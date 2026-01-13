package pyws.swyp.meeting.controller.vote;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import pyws.swyp.meeting.dto.vote.time.TimeVoteRequest;
import pyws.swyp.meeting.entity.*;
import pyws.swyp.meeting.entity.vote.TimeVote;
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
class TimeVoteControllerTest {

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
    private Long memberId1;
    private Long memberId2;
    private Long memberId3;
    private Long participantId1;
    private Long participantId2;
    private Long participantId3;

    @BeforeEach
    void setUp() {
        timeVoteRepository.deleteAll();
        dateVoteRepository.deleteAll();
        meetingParticipantRepository.deleteAll();
        meetingRepository.deleteAll();
        memberRepository.deleteAll();

        // members
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            members.add(Member.builder()
                    .email("test" + i + "@test.com")
                    .nickname("test" + i)
                    .gender(Gender.MALE)
                    .birthDate(LocalDate.of(1999, 1, 1))
                    .role(MemberRole.MEMBER)
                    .characterType(CharacterType.ACTIVE)
                    .build());
        }

        List<Member> savedMembers = memberRepository.saveAll(members);
        this.memberId1 = savedMembers.get(0).getId();
        this.memberId2 = savedMembers.get(1).getId();
        this.memberId3 = savedMembers.get(2).getId();

        // meeting
        Meeting meeting = meetingRepository.save(Meeting.builder()
                .title("테스트 모임")
                .type(MeetingType.DRINKER)
                .build());
        meeting.updateStatus(MeetingStatus.TIME_VOTING);
        meetingRepository.save(meeting);
        this.meetingId = meeting.getId();

        // participants
        List<MeetingParticipant> participants = new ArrayList<>();
        for (Member member : savedMembers) {
            participants.add(MeetingParticipant.builder()
                    .meeting(meeting)
                    .member(member)
                    .role(ParticipantRole.MEMBER)
                    .build());
        }
        meetingParticipantRepository.saveAll(participants);
        participantId1 = participants.get(0).getId();
        participantId2 = participants.get(1).getId();
        participantId3 = participants.get(2).getId();

        // auth principal
        AuthTestPrincipalContext.setMemberId(this.memberId1);
    }

    @Test
    @DisplayName("시간 투표에 성공한다.")
    void voteTimes_success() throws Exception {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        TimeVoteRequest request = new TimeVoteRequest(List.of(t1, t2));

        // when
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        List<TimeVote> votes = timeVoteRepository.findAllByMeetingParticipantId(participantId1);
        assertEquals(2, votes.size());

        List<LocalTime> times = votes.stream()
                .map(TimeVote::getTime)
                .toList();
        assertTrue(times.contains(t1));
        assertTrue(times.contains(t2));
    }

    @Test
    @DisplayName("시간 투표는 덮어쓰기 된다.")
    void voteTimes_overwrite() throws Exception {
        // given: 최초 투표 수행
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        TimeVoteRequest firstRequest = new TimeVoteRequest(List.of(t1, t2));
        TimeVoteRequest secondRequest = new TimeVoteRequest(List.of(t3));

        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // when
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isOk());

        // then
        List<TimeVote> votes = timeVoteRepository.findAllByMeetingParticipantId(participantId1);
        assertEquals(1, votes.size());
        assertEquals(t3, votes.getFirst().getTime());

        List<LocalTime> times = timeVoteRepository.findVotedTimesByMeetingId(meetingId);
        assertEquals(t3, times.getFirst());
    }

    @Test
    @DisplayName("모임이 투표 불가능한 상태면 시간 투표 시 400과 에러코드를 반환한다.")
    void voteTimes_notVotable_400() throws Exception {
        // given
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();
        meeting.updateStatus(MeetingStatus.DONE);
        meetingRepository.save(meeting);

        TimeVoteRequest request = new TimeVoteRequest(List.of(LocalTime.of(15, 0)));

        // expected
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.MEETING_NOT_VOTABLE.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEETING_NOT_VOTABLE.getMessage()));
    }

    @Test
    @DisplayName("참여하지 않은 사용자가 조회하면 404와 에러코드를 반환한다.")
    void getVotedTimes_notParticipant_404() throws Exception {
        // given
        AuthTestPrincipalContext.setMemberId(999999L);

        // expected
        mockMvc.perform(get("/api/meetings/{meetingId}/votes/times", meetingId))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEETING_PARTICIPANT_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("투표된 시간 및 투표수 조회에 성공한다.")
    void getVotedTimes_success() throws Exception {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        // m1: t1, t2
        AuthTestPrincipalContext.setMemberId(memberId1);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t1, t2)))))
                .andExpect(status().isOk());

        // m2: t2, t3
        AuthTestPrincipalContext.setMemberId(memberId2);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t2, t3)))))
                .andExpect(status().isOk());

        // expected
        mockMvc.perform(get("/api/meetings/{meetingId}/votes/times", meetingId))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.times").isArray())
                .andExpect(jsonPath("$.data.times[*].time",
                        contains(t2.toString(), t1.toString(), t3.toString())))
                .andExpect(jsonPath("$.data.times[?(@.time=='15:00')].count").value(1))
                .andExpect(jsonPath("$.data.times[?(@.time=='15:30')].count").value(2))
                .andExpect(jsonPath("$.data.times[?(@.time=='16:00')].count").value(1));
    }

    @Test
    @DisplayName("최대 투표수를 받은 시간을 조회한다. (동점 시 날짜 오름차순)")
    void getTopVotedTime_success() throws Exception {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);
        LocalTime t3 = LocalTime.of(16, 0);

        // 득표: t1=2, t2=2 (동점)
        AuthTestPrincipalContext.setMemberId(memberId1);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t1, t3)))))
                .andExpect(status().isOk());

        AuthTestPrincipalContext.setMemberId(memberId2);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t2)))))
                .andExpect(status().isOk());

        AuthTestPrincipalContext.setMemberId(memberId3);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t1, t2)))))
                .andExpect(status().isOk());

        // expected
        mockMvc.perform(get("/api/meetings/{meetingId}/votes/times/top?limit=2", meetingId))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.times[0]").value(t1.toString()))
                .andExpect(jsonPath("$.data.times[1]").value(t2.toString()));
    }

    @Test
    @DisplayName("특정 시간에 투표한 모임원 조회에 성공한다.")
    void getVotersByTime_success() throws Exception {
        // given
        LocalTime t1 = LocalTime.of(15, 0);
        LocalTime t2 = LocalTime.of(15, 30);

        AuthTestPrincipalContext.setMemberId(memberId1);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t1, t2)))))
                .andExpect(status().isOk());

        AuthTestPrincipalContext.setMemberId(memberId2);
        mockMvc.perform(post("/api/meetings/{meetingId}/votes/times", meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TimeVoteRequest(List.of(t2)))))
                .andExpect(status().isOk());

        // expected: t2에 투표한 모임원(memberId1, memberId2)
        mockMvc.perform(get("/api/meetings/{meetingId}/votes/times/{time}/voters", meetingId, t2))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.voters").isArray())
                .andExpect(jsonPath("$.data.voters.length()").value(2))
                .andExpect(jsonPath("$.data.voters[*].memberId",
                        containsInAnyOrder(memberId1.intValue(), memberId2.intValue())));
    }

    @Test
    @DisplayName("시간 형식이 올바르지 않으면 400과 필드 에러를 반환한다.")
    void getVotersByTime_invalidTimeFormat_400() throws Exception {
        // given
        String invalidTime = "25:99";

        // expected
        mockMvc.perform(get("/api/meetings/{meetingId}/votes/times/{time}/voters", meetingId, invalidTime))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT.getMessage()))
                .andExpect(jsonPath("$.data.time")
                        .value("값의 형식이 올바르지 않습니다. (입력값: " + invalidTime + ")"));
    }
}
