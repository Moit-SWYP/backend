package pyws.swyp.notification.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import pyws.swyp.notification.dto.NotificationResponse;

@SecurityRequirement(name = "auth")
@Tag(name = "Notification Api", description = "알림 및 FCM 토큰 관리 API")
public interface NotificationApi {

    @Operation(
            summary = "FCM 토큰 등록",
            description = """
                    로그인한 사용자의 FCM 디바이스 토큰을 등록합니다.
                    
                    - 앱 실행 시 또는 토큰 갱신 시 호출됩니다.
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "토큰 등록 성공")
    void registerToken(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    description = "FCM 디바이스 토큰"
            )
            @RequestParam String token
    );

    @Operation(
            summary = "FCM 토큰 해제",
            description = """
                    로그아웃 또는 앱 삭제 시 FCM 토큰을 해제합니다.
                    
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "토큰 해제 성공")
    void unregisterToken(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    description = "FCM 디바이스 토큰"
            )
            @RequestParam String token
    );

    @Operation(
            summary = "내 알림 목록 조회",
            description = """
                    로그인한 사용자의 알림 목록을 50개까지 조회합니다.
                    
                    - 최신 알림 순으로 조회됩니다.
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공")
    List<NotificationResponse> getMyNotifications(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "알림 읽음 처리",
            description = """
                    특정 알림을 읽음 상태로 변경합니다.
                    
                    - Authorization 헤더에 Access Token이 필요합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
    void markRead(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(description = "알림 ID", example = "1")
            @PathVariable Long notificationId
    );
}

