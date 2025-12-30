package pyws.swyp.global.entity;

public class UuidFormatter {
    public static String replaceUuid(String uuid) {
        return uuid.replaceFirst(
                "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
            "$1-$2-$3-$4-$5"
        );
    }
}
