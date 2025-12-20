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
import org.springframework.web.bind.annotation.PathVariable;
import pyws.swyp.member.dto.MemberResponse;
import pyws.swyp.member.dto.MemberWithdrawRequest;
import pyws.swyp.member.dto.SocialLinkRequest;
import pyws.swyp.member.entity.CharacterType;
import pyws.swyp.member.entity.SocialProvider;

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

    @Operation(
            summary = "내 캐릭터 변경",
            description =
                    """
                            현재 로그인된 회원의 캐릭터를 변경합니다.
                            
                            - 캐릭터 타입은 PathVariable로 전달됩니다.
                            - Authorization 헤더에 Access Token이 필요합니다.
                            
                            사용 가능한 캐릭터 타입:
                            - FOODIE (미식형)
                            - DRINKER (음주형)
                            - HEALER (휴식형)
                            - CULTURE_LOVER (관람형)
                            - TRAVELER (모험형)
                            - ACTIVE (활동형)
                            - TREND_SETTER (주목형)
                            - STUDYER (학습형)
                            
                            예) /api/members/me/CULTURE_LOVER
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "캐릭터 변경 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "캐릭터 변경 성공",
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
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 캐릭터 타입",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "캐릭터 타입 변환 실패",
                                    summary = "존재하지 않는 enum 값",
                                    value = """
                                            {
                                              "code": "INVALID_CHARACTER_TYPE",
                                              "message": "유효하지 않은 캐릭터 타입입니다."
                                            }
                                            """
                            )
                    }
            )
    )
    void updateCharacter(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    name = "character",
                    description = "변경할 캐릭터 타입(enum). 예: CULTURE_LOVER",
                    example = "CULTURE_LOVER"
            )
            @PathVariable("character") CharacterType character
    );

    @Operation(
            summary = "소셜 계정 연동",
            description =
                    """
                            현재 로그인된 회원 계정에 소셜 계정을 연동합니다.
                            
                            - 이미 연동된 소셜 계정인 경우 예외가 발생할 수 있습니다.
                            - Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "소셜 계정 연동 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "소셜 계정 연동 성공",
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
    @ApiResponse(
            responseCode = "409",
            description = "이미 연동된 소셜 계정",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "이미 연동됨",
                                    value = """
                                            {
                                              "code": "SOCIAL_ACCOUNT_ALREADY_LINKED",
                                              "message": "이미 연동된 소셜 계정입니다."
                                            }
                                            """
                            )
                    }
            )
    )
    void link(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Validated SocialLinkRequest request
    );

    @Operation(
            summary = "소셜 계정 연동 해제",
            description =
                    """
                            현재 로그인된 회원 계정에 연동된 소셜 계정을 해제합니다.
                            
                            - provider는 PathVariable로 전달됩니다. (예: KAKAO, NAVER)
                            - Authorization 헤더에 Access Token이 필요합니다.
                            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "소셜 계정 연동 해제 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "소셜 계정 연동 해제 성공",
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
    @ApiResponse(
            responseCode = "404",
            description = "연동된 소셜 계정을 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "연동 정보 없음",
                                    value = """
                                            {
                                              "code": "SOCIAL_ACCOUNT_NOT_FOUND",
                                              "message": "연동된 소셜 계정이 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    void unlink(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Long memberId,

            @Parameter(
                    name = "provider",
                    description = "해제할 소셜 제공자(enum). 예: KAKAO",
                    example = "KAKAO"
            )
            @PathVariable("provider") SocialProvider provider
    );
}
