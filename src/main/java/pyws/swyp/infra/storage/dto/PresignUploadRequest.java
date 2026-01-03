package pyws.swyp.infra.storage.dto;

import jakarta.validation.constraints.NotBlank;

public record PresignUploadRequest(
        @NotBlank(message = "파일을 선택해 주세요.")
        String fileName,
        @NotBlank(message = "파일 타입을 입력해 주세요.")
        String contentType
) {
}
