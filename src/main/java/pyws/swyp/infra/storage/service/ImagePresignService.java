package pyws.swyp.infra.storage.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pyws.swyp.infra.storage.dto.PresignUploadBulkRequest;
import pyws.swyp.infra.storage.dto.PresignedUploadResponse;
import pyws.swyp.infra.storage.properties.NcpObjectStorageProperties;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class ImagePresignService {

    private final S3Presigner s3Presigner;
    private final NcpObjectStorageProperties props;

    /**
     * 다중 이미지 업로드를 위한 Presigned PUT URL을 생성한다.<br>
     *
     * - 클라이언트는 각 파일별 Presigned URL을 발급받는다.<br>
     * - 실제 파일 업로드는 클라이언트 → Object Storage로 직접 수행된다.
     */
    public List<PresignedUploadResponse> generatePresignedPutUrls(PresignUploadBulkRequest request) {
        return request.files().stream()
                .map(file -> generatePresignedPutUrl(file.fileName(), file.contentType()))
                .toList();
    }

    /**
     * 단일 파일에 대한 Presigned PUT URL을 생성한다.
     */
    private PresignedUploadResponse generatePresignedPutUrl(String originalFilename, String contentType) {
        String key = generateKey(originalFilename);

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putReq)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignReq);

        return new PresignedUploadResponse(
                key,
                presigned.url().toString(),
                contentType
        );
    }

    /**
     * 이미지 조회를 위한 Presigned GET URL을 생성한다.<br>
     *
     * - Private 버킷에서도 이미지 접근 가능<br>
     * - 만료 시간이 지나면 자동으로 접근 불가
     */
    public String generatePresignedGetUrl(String imageKey) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(props.getBucket())
                .key("uploads/" + imageKey)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);

        return presigned.url().toString();
    }

    /**
     * Object Storage에 저장될 파일 Key 생성
     */
    private String generateKey(String originalFilename) {
        String ext = "";
        int idx = originalFilename.lastIndexOf('.');
        if (idx > -1) ext = originalFilename.substring(idx);
        return "uploads/" + UUID.randomUUID() + ext;
    }
}
