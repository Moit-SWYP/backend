package pyws.swyp.auth.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import pyws.swyp.auth.dto.AuthResponse;
import pyws.swyp.auth.dto.JwtResponse;
import pyws.swyp.auth.dto.LoginRequest;
import pyws.swyp.auth.dto.ReissueRequest;
import pyws.swyp.auth.dto.SignupRequest;

@Tag(name = "Auth API", description = "로그인 / 로그아웃 / 회원가입 / 토큰 재발급 API")
public interface AuthApi {

    @Operation(
            summary = "로그인",
            description =
                    """
                            로그인 요청을 처리합니다.
                            
                            - 기존 회원: JWT(access, refresh) 포함한 AuthResponse 반환
                            - 신규 회원: signupRequired = true, tokens = null 반환
                            
                            앱에서 이미 소셜 계정 인증이 완료된 상태에서 호출됩니다.
                            """
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
}
