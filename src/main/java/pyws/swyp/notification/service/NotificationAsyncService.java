package pyws.swyp.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pyws.swyp.notification.client.FcmClient;
import pyws.swyp.notification.dto.MemberToken;
import pyws.swyp.notification.dto.NotificationCommand;
import pyws.swyp.notification.dto.NotificationSend;
import pyws.swyp.notification.entity.Notification;
import pyws.swyp.notification.repository.MemberDeviceTokenRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationAsyncService {

    private final MemberDeviceTokenRepository memberDeviceTokenRepository;
    private final NotificationCommandService commandService;
    private final FcmClient fcmClient;

    /**
     * 모임원 전체를 대상으로 FCM Multicast 알림을 비동기로 발송한다.<br>
     * 발송 결과에 따라 각 회원의 알림 상태를 SENT/FAILED로 갱신한다.
     */
    @Async("notificationExecutor")
    public void createAndSendMulticast(List<Long> memberIds, NotificationCommand cmd) {
        // 모임원별 인앱 알림을 PENDING 상태로 생성한다.
        List<Notification> notifications = commandService.createNotifications(
                memberIds,
                cmd.type(),
                cmd.title(),
                cmd.body(),
                cmd.deepLink(),
                cmd.meetingId()
        );

        // 회원별 디바이스 토큰을 조회한다.
        List<MemberToken> memberTokens = memberDeviceTokenRepository.findTokensByMemberIds(memberIds);

        // memberId -> token 매핑 (멀티 디바이스 중 대표 토큰만 사용)
        Map<Long, String> tokenByMemberId = memberTokens.stream()
                .collect(Collectors.toMap(
                        MemberToken::memberId,
                        MemberToken::token,
                        (a, b) -> a)  // 기존 키값만 유지
                );

        // 토큰이 없는 회원 실패 처리
        notifications.stream()
                .filter(n -> !tokenByMemberId.containsKey(n.getMemberId()))
                .forEach(n -> n.markFailed("NO_ACTIVE_TOKEN"));

        // 실제 알림 발송 대상
        List<Notification> targets = notifications.stream()
                .filter(n -> tokenByMemberId.containsKey(n.getMemberId()))
                .toList();

        // 발송 대상이 없으면 상태만 갱신하고 종료한다.
        if (targets.isEmpty()) {
            commandService.updateStatuses(notifications);
            log.info("FCM multicast skipped (no valid tokens). meetingId={}, type={}, members={}",
                    cmd.meetingId(),
                    cmd.type(),
                    memberIds.size()
            );
            return;
        }

        // FCM Multicast 요청을 위한 토큰 리스트를 생성한다.
        List<String> tokens = targets.stream()
                .map(n -> tokenByMemberId.get(n.getMemberId()))
                .toList();

        try {
            BatchResponse batchResponse = fcmClient.sendMulticast(
                    new NotificationSend(tokens, cmd.title(), cmd.body(), cmd.data()));

            // FCM 응답 순서를 토큰 요청 순서와 매핑하여 결과를 반영한다.
            for (int i = 0; i < batchResponse.getResponses().size(); i++) {
                var response = batchResponse.getResponses().get(i);
                Notification n = targets.get(i);

                if (response.isSuccessful()) {
                    n.markSent();
                } else {
                    n.markFailed(toSafeReason(response.getException()));
                }
            }

            // 최종 알림 상태를 DB에 반영한다.
            commandService.updateStatuses(notifications);

            log.info("FCM multicast done. meetingId={}, type={}, members={}, tokens={}, success={}, fail={}",
                    cmd.meetingId(), cmd.type(), memberIds.size(), tokens.size(),
                    batchResponse.getSuccessCount(), batchResponse.getFailureCount());

        } catch (Exception e) {
            String reason = toSafeReason(e);
            targets.forEach(n -> n.markFailed(reason));
            commandService.updateStatuses(notifications);

            log.error("FCM multicast failed. meetingId={}, type={}, members={}",
                    cmd.meetingId(), cmd.type(), memberIds.size(), e);
        }
    }

    /**
     * 예외 메시지 추출
     */
    private String toSafeReason(Exception e) {
        if (e == null) {
            return "UNKNOWN";
        }
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return e.getClass().getSimpleName();
        }
        return msg.length() > 200 ? msg.substring(0, 200) : msg;
    }
}
