package pyws.swyp.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pyws.swyp.config.TestAsyncConfig;
import pyws.swyp.notification.client.FcmClient;
import pyws.swyp.notification.dto.NotificationCommand;
import pyws.swyp.notification.dto.NotificationSend;
import pyws.swyp.notification.entity.MemberDeviceToken;
import pyws.swyp.notification.entity.Notification;
import pyws.swyp.notification.entity.NotificationStatus;
import pyws.swyp.notification.repository.MemberDeviceTokenRepository;
import pyws.swyp.notification.repository.NotificationRepository;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
class NotificationSenderTest {

    @Autowired
    NotificationSender notificationSender;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    MemberDeviceTokenRepository memberDeviceTokenRepository;

    @MockitoBean
    FcmClient fcmClient;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        memberDeviceTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("토큰이 없는 회원은 FAILED 처리되고 FCM은 호출되지 않는다.")
    void noTokenMembers_areMarkedFailed() throws FirebaseMessagingException {
        // given
        Long meetingId = 1L;
        List<Long> memberIds = List.of(1L, 2L);

        NotificationCommand cmd = NotificationCommand.voteStarted(meetingId);

        // when
        notificationSender.createAndSendMulticast(memberIds, cmd);

        // then
        List<Notification> notifications = notificationRepository.findAll();
        assertEquals(2,  notifications.size());
        assertThat(notifications)
                .allMatch(n -> n.getStatus() == NotificationStatus.FAILED);

        verify(fcmClient, never()).sendMulticast(any());
    }

    @Test
    @DisplayName("토큰이 있는 회원은 SENT 처리된다.")
    void tokenMembers_areSent() throws Exception {
        // given
        Long meetingId = 1L;
        Long memberId = 1L;

        MemberDeviceToken deviceToken = MemberDeviceToken.builder()
                .memberId(memberId)
                .token("token-1")
                .build();
        memberDeviceTokenRepository.save(deviceToken);

        BatchResponse batchResponse = mock(BatchResponse.class);
        SendResponse response = mock(SendResponse.class);

        when(response.isSuccessful()).thenReturn(true);
        when(batchResponse.getResponses()).thenReturn(List.of(response));
        when(batchResponse.getSuccessCount()).thenReturn(1);
        when(batchResponse.getFailureCount()).thenReturn(0);

        when(fcmClient.sendMulticast(any(NotificationSend.class))).thenReturn(batchResponse);

        NotificationCommand cmd = NotificationCommand.voteStarted(meetingId);

        // when
        notificationSender.createAndSendMulticast(List.of(memberId), cmd);

        // then
        List<Notification> all = notificationRepository.findAll();
        Notification notification = all.getFirst();

        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    @DisplayName("일부만 토큰이 있으면 토큰 없는 회원은 FAILED, 토큰 있는 회원은 FCM 응답대로 처리된다.")
    void partialTokenMembers_areHandledCorrectly() throws Exception {
        // given
        Long meetingId = 1L;
        Long member1 = 1L; // token 있음 -> 성공
        Long member2 = 2L; // token 없음 -> NO_ACTIVE_TOKEN 실패
        Long member3 = 3L; // token 있음 -> 실패

        // 토큰 저장: 1,3만
        memberDeviceTokenRepository.save(MemberDeviceToken.builder()
                .memberId(member1)
                .token("token-1")
                .build());
        memberDeviceTokenRepository.save(MemberDeviceToken.builder()
                .memberId(member3)
                .token("token-3")
                .build());

        // FCM 응답: token-1 성공, token-3 실패
        BatchResponse batchResponse = mock(BatchResponse.class);

        SendResponse r1 = mock(SendResponse.class);
        when(r1.isSuccessful()).thenReturn(true);

        SendResponse r2 = mock(SendResponse.class);
        when(r2.isSuccessful()).thenReturn(false);
        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        when(ex.getMessage()).thenReturn("INVALID_ARGUMENT");
        when(r2.getException()).thenReturn(ex);

        when(batchResponse.getResponses()).thenReturn(List.of(r1, r2));
        when(batchResponse.getSuccessCount()).thenReturn(1);
        when(batchResponse.getFailureCount()).thenReturn(1);

        when(fcmClient.sendMulticast(any(NotificationSend.class))).thenReturn(batchResponse);

        NotificationCommand cmd = NotificationCommand.voteStarted(meetingId);

        // when
        notificationSender.createAndSendMulticast(List.of(member1, member2, member3), cmd);

        // then
        List<Notification> all = notificationRepository.findAll();
        assertEquals(3, all.size());

        Notification n1 = findByMemberId(all, member1);
        Notification n2 = findByMemberId(all, member2);
        Notification n3 = findByMemberId(all, member3);

        assertEquals(NotificationStatus.SENT, n1.getStatus());
        assertEquals(NotificationStatus.FAILED, n2.getStatus());
        assertEquals(NotificationStatus.FAILED, n3.getStatus());

        assertEquals("NO_ACTIVE_TOKEN", n2.getFailReason());

        ArgumentCaptor<NotificationSend> captor = ArgumentCaptor.forClass(NotificationSend.class);
        verify(fcmClient).sendMulticast(captor.capture());

        NotificationSend sent = captor.getValue();
        assertEquals(List.of("token-1", "token-3"), sent.tokens());
    }

    private static Notification findByMemberId(List<Notification> list, Long memberId) {
        return list.stream()
                .filter(n -> memberId.equals(n.getMemberId()))
                .findFirst()
                .orElseThrow();
    }
}