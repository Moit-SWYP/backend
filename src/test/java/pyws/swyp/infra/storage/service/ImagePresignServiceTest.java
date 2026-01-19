package pyws.swyp.infra.storage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pyws.swyp.infra.storage.dto.PresignUploadBulkRequest;
import pyws.swyp.infra.storage.dto.PresignUploadRequest;
import pyws.swyp.infra.storage.dto.PresignedUploadResponse;
import pyws.swyp.infra.storage.properties.NcpObjectStorageProperties;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class ImagePresignServiceTest {

    @Mock
    S3Presigner s3Presigner;

    @Mock
    NcpObjectStorageProperties props;

    @InjectMocks
    ImagePresignService imagePresignService;

    @Test
    @DisplayName("다중 이미지 Presigned PUT URL을 생성한다.")
    void shouldGeneratePresignedPutUrlsForMultipleImages() throws MalformedURLException {
        // given
        given(props.getBucket()).willReturn("test-bucket");

        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        given(mockPresigned.url()).willReturn(URI.create("https://presigned.test").toURL());

        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(mockPresigned);

        PresignUploadBulkRequest request = new PresignUploadBulkRequest(
                List.of(
                        new PresignUploadRequest("a.jpg", "image/jpeg"),
                        new PresignUploadRequest("b.png", "image/png")
                )
        );

        // when
        List<PresignedUploadResponse> result =
                imagePresignService.generatePresignedPutUrls(request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().presignedUrl()).contains("https://presigned.test");
        assertThat(result.getFirst().imageKey()).startsWith("uploads/");
    }

    @Test
    @DisplayName("Presigned GET URL을 생성한다.")
    void shouldGeneratePresignedGetUrl() throws MalformedURLException {
        // given
        given(props.getBucket()).willReturn("test-bucket");

        PresignedGetObjectRequest mockPresigned = mock(PresignedGetObjectRequest.class);
        given(mockPresigned.url()).willReturn(URI.create("https://get.test").toURL());

        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willReturn(mockPresigned);

        // when
        String url = imagePresignService.generatePresignedGetUrl("abc.jpg");

        // then
        assertThat(url).isEqualTo("https://get.test");
    }
}
