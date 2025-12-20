package pyws.swyp.member.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;

@SecurityRequirement(name = "auth")
@Tag(name = "Member API", description = "회원 정보 조회 / 회원 탈퇴 API")
public interface MemberApi {

    @Operation(
            summary = "내 정보 조회",
            description =
                    """
                            현재 로그인된 회원의 정보를 조회합니다.
                            
                            조회 항목:
                            - 이메일
                            - 닉네임
                            - 생년월일
                            - 성별
                            - 연동된 소셜 계정 목록
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "내 정보 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "내 정보 조회 성공",
                                    summary = "로그인된 회원 정보",
                                    value = """
                                            {
                                              "code": "SUCCESS",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "email": "user@example.com",
                                                "nickname": "스윕유저",
                                                "birthDate": "1998-05-21",
                                                "gender": "MALE",
                                                "socialAccounts": [
                                                  {
                                                    "provider": "KAKAO"
                                                  },
                                                  {
                                                    "provider": "NAVER"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    }
            )
    )
    MemberResponse getMember(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId
    );

    @Operation(
            summary = "회원 탈퇴",
            description =
                    """
                            회원 탈퇴를 처리합니다.
                            
                            - 회원은 논리 삭제 처리됩니다.
                            - 탈퇴 사유가 기록됩니다.
                            - 연동된 소셜 계정은 비활성화됩니다.
                            - Redis에 저장된 Refresh Token이 삭제되어 즉시 로그아웃 처리됩니다.
                            
                            탈퇴 사유:
                            1. 일정 생성이 불편해요.
                            2. 원하는 기능이 없어요.
                            3. 버그가 자꾸 발생해요.
                            4. 기타 (직접 입력)
                            
                            Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "회원 탈퇴 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "회원 탈퇴 성공",
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
    void withdrawMember(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Validated MemberWithdrawRequest request
    );
}
