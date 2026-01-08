package pyws.swyp.notification.client;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Component;
import pyws.swyp.notification.dto.NotificationSend;

@Component
public class FcmClient {

    public BatchResponse sendMulticast(NotificationSend request)
            throws FirebaseMessagingException {

        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(request.tokens())
                .setNotification(notification)
                .putAllData(request.data())
                .build();

        return FirebaseMessaging.getInstance().sendEachForMulticast(message);
    }
}
