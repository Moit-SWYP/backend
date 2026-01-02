package pyws.swyp.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.global.error.ErrorCode;
import pyws.swyp.notification.dto.NotificationResponse;
import pyws.swyp.notification.entity.MemberDeviceToken;
import pyws.swyp.notification.entity.Notification;
import pyws.swyp.notification.repository.MemberDeviceTokenRepository;
import pyws.swyp.notification.repository.NotificationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationUserService {

    private final NotificationRepository notificationRepository;
    private final MemberDeviceTokenRepository memberDeviceTokenRepository;

    /**
     * 알림을 실제로 보내기 위한 디바이스 주소를 등록한다.
     */
    @Transactional
    public void registerToken(Long memberId, String token) {
        if (memberDeviceTokenRepository.existsByMemberIdAndToken(memberId, token)) {
            throw ErrorCode.DEVICE_TOKEN_ALREADY_REGISTERED.toException();
        }

        MemberDeviceToken deviceToken = MemberDeviceToken.builder()
                .memberId(memberId)
                .token(token)
                .build();

        memberDeviceTokenRepository.save(deviceToken);
    }

    /**
     * 디바이스 주소를 삭제한다.
     */
    @Transactional
    public void unregisterToken(Long memberId, String token) {
        MemberDeviceToken deviceToken = memberDeviceTokenRepository.findByMemberIdAndToken(memberId, token)
                .orElseThrow(ErrorCode.DEVICE_TOKEN_NOT_FOUND::toException);
        memberDeviceTokenRepository.delete(deviceToken);
    }

    /**
     * 사용자의 최근 알림 목록을 최대 50개까지 조회한다.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long memberId) {
        return notificationRepository.findTop50ByMemberIdOrderByCreatedAtDescIdDesc(memberId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     */
    @Transactional
    public void markRead(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(ErrorCode.NOTIFICATION_NOT_FOUND::toException);

        if (!notification.getMemberId().equals(memberId)) {
            throw ErrorCode.NOTIFICATION_FORBIDDEN.toException();
        }

        notification.markRead();
    }
}

