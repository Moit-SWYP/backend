package pyws.swyp.member.controller.friend;

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
import pyws.swyp.global.jwt.JwtProvider;
import pyws.swyp.member.dto.friend.GroupCreateRequest;
import pyws.swyp.member.service.friend.FriendService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Import({AuthPrincipalTestConfig.class})
@RequiredArgsConstructor
public class FriendControllerTest {
    private final MockMvc mockMvc;

    @MockitoBean
    FriendService friendService;

    @MockitoBean
    JwtProvider jwtProvider;

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @DisplayName("초대에 필요한 친구 목록 조회에 성공한다")
    void 친구_목록_조회_성공() throws Exception{
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/members/friendships"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(friendService, times(1)).getFriends(memberId);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("그룹 초대에 필요한 그룹 목록 조회에 성공한다.")
    void 친구_그룹_목록_조회_성공() throws Exception{
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/members/groups"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(friendService, times(1)).getFriendGroups(memberId);
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("그룹 생성에 성공한다.")
    void 친구_그룹_생성_성공() throws Exception{
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        GroupCreateRequest request = new GroupCreateRequest(
                "모잉이들",
                List.of(2L, 3L)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/members/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // then
        verify(friendService, times(1))
                .createFriendGroup(eq(memberId), any(GroupCreateRequest.class));
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("그룹명이 Blank라면 그룹 생성에 실패한다.")
    void 친구_그룹_생성_실패_그룹명_공백() throws Exception{
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        GroupCreateRequest request = new GroupCreateRequest(
                " ",
                List.of(2L, 3L)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/members/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verify(friendService, never())
                .createFriendGroup(any(), any());
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("멤버에 1명 이하로 있다면 그룹 생성에 성공한다.")
    void 친구_그룹_생성_실패_그룹_멤버_1명_이하() throws Exception{
        // given
        Long memberId = 1L;
        AuthTestPrincipalContext.setMemberId(memberId);

        GroupCreateRequest request = new GroupCreateRequest(
                "모잉이들",
                List.of(2L)
        );

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/members/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        // then
        verify(friendService, never())
                .createFriendGroup(any(), any());
        Integer status = mvcResult.getResponse().getStatus();
        assertThat(status).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
