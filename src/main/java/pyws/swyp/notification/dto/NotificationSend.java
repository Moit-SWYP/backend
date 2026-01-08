package pyws.swyp.notification.dto;

import java.util.List;
import java.util.Map;

public record NotificationSend(
        List<String> tokens,
        String title,
        String body,
        Map<String, String> data
) {
}
