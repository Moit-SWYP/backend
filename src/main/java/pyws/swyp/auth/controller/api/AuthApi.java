package pyws.swyp.auth.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.JwtResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.ReissueRequest;
import pyws.swyp.auth.dto.SignupRequest;

@SecurityRequirement(name = "auth")
@Tag(name = "Auth API", description = "로그인 / 로그아웃 / 회원가입 / 토큰 재발급 API")
public interface AuthApi {

    @Operation(
            summary = "로그인",
            description =
                    """
                            로그인 요청을 처리합니다.
                            
                            - 기존 회원: JWT(access, refresh) 포함한 AuthResponse 반환
                            - 신규 회원: signupRequired = true, tokens = null 반환
                            
                            토큰 정책:
                            - Access Token: 15분 유효 (API 인증에 사용)
                            - Refresh Token: 7일 유효 (Access Token 만료 시 재발급에 사용)
                            
                            앱에서 이미 소셜 계정 인증이 완료된 상태에서 호출됩니다.
                            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "로그인 요청",
                                    value = """
                                            {
                                              "socialProvider": "KAKAO",
                                              "socialId": "existing-social-id-123",
                                              "email": "existing@example.com"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "기존 회원 - 로그인 성공",
                                    summary = "기존 회원인 경우",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "signupRequired": false,
                                                "tokens": {
                                                  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                }
                                              }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "신규 회원 - 회원가입 필요",
                                    summary = "가입되지 않은 소셜 계정인 경우",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "signupRequired": true,
                                                "tokens": null
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    AuthResponse login(@RequestBody @Validated LoginRequest request);

    @Operation(
            summary = "회원가입",
            description =
                    """
                            신규 회원의 회원가입을 처리합니다.
                            
                            요청 구조:
                            - login: 소셜 정보 (socialProvider, socialId, email)
                            - nickname, birthdate, gender
                            
                            응답:
                            - signupRequired: false
                            - tokens: 로그인 완료 후 발급된 JWT(access, refresh)
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "회원가입 성공",
                                    summary = "신규 회원 가입 후 JWT 발급",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "signupRequired": false,
                                                "tokens": {
                                                  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                                }
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    AuthResponse signup(@RequestBody @Validated SignupRequest request);

    @Operation(
            summary = "로그아웃",
            description =
                    """
                            현재 로그인된 사용자를 로그아웃 처리합니다.
                            
                            - Redis에 저장된 Refresh Token을 삭제합니다.
                            - Access Token은 만료 시점까지 유효하지만,
                              이후 재발급은 불가능합니다.
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    void logout(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "토큰 재발급",
            description =
                    """
                            Refresh Token을 이용해 Access / Refresh Token을 재발급합니다.
                            
                            - Refresh Token 검증 및 Redis 저장값과의 일치 여부를 확인합니다.
                            - 유효한 경우, 기존 토큰은 무효화되고 새로운 토큰 쌍이 발급됩니다.
                            - Access Token 만료 시 호출됩니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "재발급 성공",
                                    summary = "Access / Refresh Token 재발급",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    JwtResponse reissue(@RequestBody @Validated ReissueRequest request);

    @Operation(
            summary = "카카오 연결 해제 콜백",
            description =
                    """
                    카카오 서버가 사용자의 서비스 연결 해제 이벤트 발생 시 호출하는 콜백 API입니다.
    
                    - 호출 주체: 카카오 서버
                    - 인증 방식: Authorization 헤더 (KakaoAK {PRIMARY_ADMIN_KEY})
                    - 정상 처리 시 HTTP 200(OK) 반환
    
                    referrer_type 값:
                    - ACCOUNT_DELETE: 카카오 계정 탈퇴
                    - FORCED_ACCOUNT_DELETE: 장기 휴면 또는 고객센터에 의한 강제 탈퇴
                    - UNLINK_FROM_ADMIN: 카카오 관리자에 의한 탈퇴
                    - UNLINK_FROM_APPS: 카카오 계정 페이지에서 서비스 연결 해제
                    - INCOMPLETE_SIGN_UP: 가입 미완료 사용자 연결 해제
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "카카오 연결 해제 콜백 처리 성공"
    )
    @PostMapping("/oauth/kakao/unlink")
    ResponseEntity<Void> handleKakaoUnlinkCallback(
            @Parameter(
                    name = "Authorization",
                    description = "카카오 어드민 키 (형식: KakaoAK {PRIMARY_ADMIN_KEY})",
                    required = true,
                    example = "KakaoAK a1b2c3d4e5"
            )
            @RequestHeader("Authorization") String authorization,

            @Parameter(
                    name = "app_id",
                    description = "연결 해제 요청이 발생한 카카오 앱 ID",
                    required = true,
                    example = "123456789"
            )
            @RequestParam("app_id") String appId,

            @Parameter(
                    name = "user_id",
                    description = "카카오 회원 번호 (user_id)",
                    required = true,
                    example = "987654321"
            )
            @RequestParam("user_id") String userId,

            @Parameter(
                    name = "referrer_type",
                    description = "연결 해제 요청 경로",
                    required = true,
                    example = "UNLINK_FROM_APPS"
            )
            @RequestParam("referrer_type") String referrerType
    );

    @Operation(
            summary = "네이버 연결 해제 알림 콜백",
            description =
                    """
                    네이버 서버가 사용자의 서비스 연결 해제 이벤트 발생 시 호출하는 콜백 API입니다.
    
                    - 호출 주체: 네이버 서버
                    - 인증 방식: 요청 파라미터 기반 무결성 검증
                    - 정상 처리 시 HTTP 204(No Content) 반환
    
                    검증 요소:
                    - clientId
                    - encryptUniqueId (AES128/CBC 암호화)
                    - timestamp (Unix epoch second)
                    - signature (HMAC 서명)
                    """
    )
    @ApiResponse(
            responseCode = "204",
            description = "네이버 연결 해제 콜백 처리 성공"
    )
    @PostMapping("/oauth/naver/unlink")
    ResponseEntity<Void> handleNaverUnlinkCallback(
            @Parameter(
                    description = "네이버 애플리케이션 Client ID",
                    required = true,
                    example = "naver-client-id"
            )
            @RequestParam("clientId") String clientId,

            @Parameter(
                    description = "암호화된 네이버 이용자 고유 식별자",
                    required = true,
                    example = "hjDkQ1h_FNFiklPyEKBZwbwE"
            )
            @RequestParam("encryptUniqueId") String encryptUniqueId,

            @Parameter(
                    description = "요청 시점의 Unix epoch time (second)",
                    required = true,
                    example = "1693877406"
            )
            @RequestParam("timestamp") String timestamp,

            @Parameter(
                    description = "요청 무결성 검증을 위한 HMAC 서명 값",
                    required = true,
                    example = "XUGURE_KaNSs-Y0"
            )
            @RequestParam("signature") String signature
    );

}
