package pyws.swyp.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pyws.swyp.global.error.CustomException;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.notification.dto.NotificationResponse;
import pyws.swyp.notification.entity.MemberDeviceToken;
import pyws.swyp.notification.entity.Notification;
import pyws.swyp.notification.entity.NotificationStatus;
import pyws.swyp.notification.entity.NotificationType;
import pyws.swyp.notification.repository.MemberDeviceTokenRepository;
import pyws.swyp.notification.repository.NotificationRepository;

@SpringBootTest
class NotificationUserServiceTest {

    @Autowired
    NotificationUserService notificationUserService;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    MemberDeviceTokenRepository memberDeviceTokenRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        memberDeviceTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("신규 토큰이면 저장된다")
    void registerToken_savesNewToken() {
        // given
        Long memberId = 1L;
        String token = "token-1";

        // when
        notificationUserService.registerToken(memberId, token);

        // then
        assertTrue(memberDeviceTokenRepository.existsByMemberIdAndToken(memberId, token));
    }

    @Test
    @DisplayName("이미 등록된 토큰이면 예외가 발생한다")
    void registerToken_throwsWhenAlreadyRegistered() {
        // given
        Long memberId = 1L;
        String token = "token-1";

        memberDeviceTokenRepository.save(MemberDeviceToken.builder()
                .memberId(memberId)
                .token(token)
                .build());

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> notificationUserService.registerToken(memberId, token));

        // then
        assertEquals(ErrorCode.DEVICE_TOKEN_ALREADY_REGISTERED, ex.getErrorCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("존재하는 토큰이면 삭제된다")
    void unregisterToken_deletesExistingToken() {
        // given
        Long memberId = 1L;
        String token = "token-1";

        memberDeviceTokenRepository.save(MemberDeviceToken.builder()
                .memberId(memberId)
                .token(token)
                .build());

        assertTrue(memberDeviceTokenRepository.existsByMemberIdAndToken(memberId, token));

        // when
        notificationUserService.unregisterToken(memberId, token);

        // then
        assertFalse(memberDeviceTokenRepository.existsByMemberIdAndToken(memberId, token));
    }

    @Test
    @DisplayName("토큰이 없으면 예외가 발생한다")
    void unregisterToken_throwsWhenNotFound() {
        // given
        Long memberId = 1L;
        String token = "token-x";

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> notificationUserService.unregisterToken(memberId, token));

        // then
        assertEquals(ErrorCode.DEVICE_TOKEN_NOT_FOUND, ex.getErrorCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("최신순으로 최대 50개까지 조회된다")
    void getMyNotifications_returnsLatest50() {
        // given
        Long memberId = 1L;

        for (int i = 1; i <= 60; i++) {
            notificationRepository.save(Notification.builder()
                    .memberId(memberId)
                    .status(NotificationStatus.PENDING)
                    .type(NotificationType.VOTE_STARTED)
                    .title("t" + i)
                    .body("b" + i)
                    .build());
        }
        notificationRepository.flush();

        // when
        List<NotificationResponse> responses = notificationUserService.getMyNotifications(memberId);

        // then
        assertEquals(50, responses.size());
        assertEquals("t60", responses.getFirst().title());
        assertEquals("t11", responses.getLast().title());
    }

    @Test
    @DisplayName("내 알림이면 읽음 처리된다")
    void markRead_marksReadWhenOwner() {
        // given
        Long memberId = 1L;

        Notification notification = Notification.builder()
                .memberId(memberId)
                .status(NotificationStatus.SENT)
                .type(NotificationType.VOTE_STARTED)
                .title("t")
                .body("b")
                .build();
        Notification n = notificationRepository.save(notification);

        // when
        notificationUserService.markRead(memberId, n.getId());

        // then
        Notification updated = notificationRepository.findById(n.getId()).orElseThrow();
        assertSame(NotificationStatus.READ, updated.getStatus());
    }

    @Test
    @DisplayName("알림이 없으면 NOT_FOUND 예외가 발생한다")
    void markRead_throwsWhenNotificationNotFound() {
        // given
        Long memberId = 1L;
        Long notiId = 999L;

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> notificationUserService.markRead(memberId, notiId));

        // then
        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.getErrorCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    @DisplayName("내 알림이 아니면 FORBIDDEN 예외가 발생한다")
    void markRead_throwsWhenForbidden() {
        // given
        Long ownerId = 1L;
        Long otherId = 2L;

        Notification n = notificationRepository.save(Notification.builder()
                .memberId(ownerId)
                .status(NotificationStatus.SENT)
                .type(NotificationType.VOTE_STARTED)
                .title("t")
                .body("b")
                .build());

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> notificationUserService.markRead(otherId, n.getId()));

        // then
        assertEquals(ErrorCode.NOTIFICATION_FORBIDDEN, ex.getErrorCode());
        assertNotNull(ex.getMessage());
    }
}
