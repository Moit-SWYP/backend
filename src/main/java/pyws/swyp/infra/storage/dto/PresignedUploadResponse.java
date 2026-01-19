package pyws.swyp.infra.storage.dto;

public record PresignedUploadResponse(
        String imageKey,
        String presignedUrl,
        String contentType
) {
}
