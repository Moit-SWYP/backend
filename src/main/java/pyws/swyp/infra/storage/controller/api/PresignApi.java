package pyws.swyp.infra.storage.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import pyws.swyp.infra.storage.dto.PresignUploadBulkRequest;
import pyws.swyp.infra.storage.dto.PresignedUploadResponse;

@SecurityRequirement(name = "auth")
@Tag(name = "Presign API", description = "파일 업로드용 Presigned URL API")
public interface PresignApi {

    @Operation(
            summary = "이미지 업로드 Presigned PUT URL 발급",
            description = """
                    다중 이미지 업로드를 위한 Presigned PUT URL을 발급합니다.
                    
                    - 각 파일별로 Presigned URL이 생성됩니다.
                    - 실제 파일 업로드는 클라이언트가 Object Storage로 직접 수행합니다.
                    - 업로드 시 각 URL에 Content-Type 헤더를 반드시 포함해야 합니다.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Presigned URL 발급 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PresignedUploadResponse.class)),
                    examples = @ExampleObject(
                            name = "success",
                            summary = "성공 응답 예시 (공통 응답 래퍼 포함)",
                            value = """
                                    {
                                      "code": "SUCCESS",
                                      "message": "요청이 성공적으로 처리되었습니다.",
                                      "data": [
                                        {
                                          "imageKey": "uploads/6b38a775-e777-46a2-9de7-e6f88dacedad.jpg",
                                          "presignedUrl": "https://kr.object.ncloudstorage.com/moit-images/uploads/6b38a775-e777-46a2-9de7-e6f88dacedad.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260103T161219Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Credential=***%2F20260103%2Fkr-standard%2Fs3%2Faws4_request&X-Amz-Expires=600&X-Amz-Signature=***",
                                          "contentType": "image/jpeg"
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    List<PresignedUploadResponse> presign(
            @RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PresignUploadBulkRequest.class),
                            examples = @ExampleObject(
                                    name = "request",
                                    summary = "요청 예시",
                                    value = """
                                            {
                                              "files": [
                                                {
                                                  "fileName": "profile.jpg",
                                                  "contentType": "image/jpeg"
                                                },
                                                {
                                                  "fileName": "meeting.png",
                                                  "contentType": "image/png"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            PresignUploadBulkRequest request
    );
}
