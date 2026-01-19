package pyws.swyp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import pyws.swyp.infra.storage.service.ImagePresignService;

@TestConfiguration
public class TestPresignConfig {

    @Bean
    @Primary
    ImagePresignService imagePresignService() {
        return new ImagePresignService(null, null) {
            @Override
            public String generatePresignedGetUrl(String imageKey) {
                return "https://test.local/presigned?key=" + imageKey;
            }
        };
    }
}
