package pyws.swyp.notification.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.notification.controller.api.NotificationApi;
import pyws.swyp.notification.dto.NotificationResponse;
import pyws.swyp.notification.service.NotificationUserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationUserService notificationUserService;

    @PostMapping("/tokens")
    public void registerToken(
            @AuthenticationPrincipal Long memberId,
            @RequestParam String token
    ) {
        notificationUserService.registerToken(memberId, token);
    }

    @DeleteMapping("/tokens")
    public void unregisterToken(
            @AuthenticationPrincipal Long memberId,
            @RequestParam String token
    ) {
        notificationUserService.unregisterToken(memberId, token);
    }

    @GetMapping
    public List<NotificationResponse> getMyNotifications(@AuthenticationPrincipal Long memberId) {
        return notificationUserService.getMyNotifications(memberId);
    }

    @PostMapping("/{notificationId}/read")
    public void markRead(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long notificationId
    ) {
        notificationUserService.markRead(memberId, notificationId);
    }
}
