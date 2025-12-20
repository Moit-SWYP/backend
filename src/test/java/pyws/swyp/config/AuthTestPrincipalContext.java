package pyws.swyp.config;

public final class AuthTestPrincipalContext {

    private static final ThreadLocal<Long> MEMBER_ID = new ThreadLocal<>();

    private AuthTestPrincipalContext() {}

    public static void setMemberId(Long memberId) {
        MEMBER_ID.set(memberId);
    }

    public static Long getMemberId() {
        return MEMBER_ID.get();
    }

    public static void clear() {
        MEMBER_ID.remove();
    }
}
