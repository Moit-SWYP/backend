package pyws.swyp.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

@TestConfiguration
@EnableAsync
public class TestAsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        return new SyncTaskExecutor();
    }
}

