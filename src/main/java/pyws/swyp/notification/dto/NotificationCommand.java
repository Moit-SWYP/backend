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
        NotificationType type = NotificationType.VOTE_STARTED;
        return new NotificationCommand(
                type,
                "투표가 시작됐어요.",
                "모임 투표에 참여해 주세요!",
                deepLink,
                meetingId,
                Map.of(
                        "type", type.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand dateVoteConfirmed(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId;
        NotificationType type = NotificationType.DATE_VOTE_RESULT_CONFIRMED;
        return new NotificationCommand(
                type,
                "모임 날짜 투표 결과가 확정됐어요.",
                "모임 날짜가 확정되었습니다.",
                deepLink,
                meetingId,
                Map.of(
                        "type", type.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand timeVoteConfirmed(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId;
        NotificationType type = NotificationType.TIME_VOTE_RESULT_CONFIRMED;
        return new NotificationCommand(
                type,
                "모임 시간 투표 결과가 확정됐어요.",
                "모임 시간이 확정되었습니다.",
                deepLink,
                meetingId,
                Map.of(
                        "type", type.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand dateVoteReminder(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId + "/votes/date";
        NotificationType type = NotificationType.DATE_VOTE_REMINDER;
        return new NotificationCommand(
                type,
                "아직 날짜 투표를 안 하셨어요!",
                "모임 날짜 투표에 참여해 주세요.",
                deepLink,
                meetingId,
                Map.of(
                        "type", type.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand timeVoteReminder(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId + "/votes/time";
        NotificationType type = NotificationType.TIME_VOTE_REMINDER;
        return new NotificationCommand(
                type,
                "아직 시간 투표를 안 하셨어요!",
                "모임 시간 투표에 참여해 주세요.",
                deepLink,
                meetingId,
                Map.of(
                        "type", type.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }

    public static NotificationCommand recordReminder(Long meetingId) {
        String deepLink = "moit://meetings/" + meetingId + "/record";
        NotificationType type = NotificationType.RECORD_REMINDER;
        return new NotificationCommand(
                type,
                "아직 모임 후기를 남기지 않았어요!",
                "모임 후기를 작성해 주세요.",
                deepLink,
                meetingId,
                Map.of(
                        "type", type.name(),
                        "meetingId", meetingId.toString(),
                        "deepLink", deepLink
                )
        );
    }
}

