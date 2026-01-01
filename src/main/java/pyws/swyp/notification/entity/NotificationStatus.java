package pyws.swyp.notification.entity;

public enum NotificationStatus {
    PENDING,   // 생성됨(발송 전)
    SENT,      // 발송 성공
    FAILED,    // 발송 실패
    READ       // 사용자 읽음(인앱 알림함 기준)
}
