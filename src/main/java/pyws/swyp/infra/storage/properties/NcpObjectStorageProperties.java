package pyws.swyp.infra.storage.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ncp.object-storage")
public class NcpObjectStorageProperties {

    private String endpoint;
    private String bucket;
    private String region;
    private String accessKey;
    private String secretKey;
}
