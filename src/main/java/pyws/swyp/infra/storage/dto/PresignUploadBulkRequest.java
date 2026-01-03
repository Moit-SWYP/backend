package pyws.swyp.infra.storage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PresignUploadBulkRequest(
        @NotEmpty(message = "파일을 선택해 주세요.")
        @Size(max = 5, message = "한 번에 최대 5개까지 요청할 수 있습니다.")
        List<@Valid PresignUploadRequest> files
) {
}
