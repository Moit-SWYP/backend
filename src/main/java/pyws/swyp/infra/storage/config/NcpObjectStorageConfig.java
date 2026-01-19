package pyws.swyp.infra.storage.config;

import java.net.URI;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pyws.swyp.infra.storage.properties.NcpObjectStorageProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(NcpObjectStorageProperties.class)
public class NcpObjectStorageConfig {

    @Bean
    public S3Presigner s3Presigner(NcpObjectStorageProperties props) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                props.getAccessKey(),
                props.getSecretKey()
        );

        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Presigner.builder()
                .serviceConfiguration(s3Config)
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(props.getRegion()))
                .endpointOverride(URI.create(props.getEndpoint()))
                .build();
    }
}
