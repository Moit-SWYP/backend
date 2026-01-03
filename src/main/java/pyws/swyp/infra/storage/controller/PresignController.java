package pyws.swyp.infra.storage.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.infra.storage.controller.api.PresignApi;
import pyws.swyp.infra.storage.dto.PresignUploadBulkRequest;
import pyws.swyp.infra.storage.dto.PresignedUploadResponse;
import pyws.swyp.infra.storage.service.ImagePresignService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/presign")
public class PresignController implements PresignApi {

    private final ImagePresignService imagePresignService;

    @PostMapping
    public List<PresignedUploadResponse> presign(@RequestBody @Validated PresignUploadBulkRequest request) {
        return imagePresignService.generatePresignedPutUrls(request);
    }
}
