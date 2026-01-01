package pyws.swyp.notification.dto;

import java.util.Map;
import pyws.swyp.notification.entity.NotificationType;

public record NotificationCommand(
        NotificationType type,
        String title,
        String body,
        String deepLink,
        Long meetingId,
        Map<String, String> data
) {
    public static NotificationCommand voteStarted(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId + "/votes";
        return new NotificationCommand(
                NotificationType.VOTE_STARTED,
                "투표가 시작됐어요.",
                "모임 투표에 참여해 주세요!",
                deepLink,
                meetingId,
                Map.of(
                        "type", NotificationType.VOTE_STARTED.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand dateVoteConfirmed(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId;
        return new NotificationCommand(
                NotificationType.DATE_VOTE_RESULT_CONFIRMED,
                "모임 날짜 투표 결과가 확정됐어요.",
                "모임 날짜가 확정되었습니다.",
                deepLink,
                meetingId,
                Map.of(
                        "type", NotificationType.DATE_VOTE_RESULT_CONFIRMED.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand timeVoteConfirmed(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId;
        return new NotificationCommand(
                NotificationType.TIME_VOTE_RESULT_CONFIRMED,
                "모임 시간 투표 결과가 확정됐어요.",
                "모임 시간이 확정되었습니다.",
                deepLink,
                meetingId,
                Map.of(
                        "type", NotificationType.TIME_VOTE_RESULT_CONFIRMED.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }
}

