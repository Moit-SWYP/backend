package pyws.swyp.meeting.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import pyws.swyp.meeting.dto.HomeResponse;

@SecurityRequirement(name = "auth")
@Tag(name = "Home API")
public interface HomeApi {
    @Operation(
            summary = "홈 화면 조회",
            description =
                    """
                            홈 화면 조회에 필요한 데이터를 제공합니다.
                            7일 이내 예정된 모임 리스트 날짜 오름차순 조회
                            대기 중인 모임 리스트 최대 3개 조회
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "홈 화면 조회 성공"
    )
    HomeResponse getHomeInfo(@AuthenticationPrincipal Long memberId);

}
