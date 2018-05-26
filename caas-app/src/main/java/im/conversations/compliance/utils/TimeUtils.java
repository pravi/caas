package im.conversations.compliance.utils;

import java.time.Duration;
import java.time.Instant;

public class TimeUtils {
    public static String getTimeSince(Instant then) {
        Instant now = Instant.now();
        Duration duration = Duration.between(then, now);
        String time;
        if (duration.getSeconds() < 5) {
            time = "just now";
        } else if (duration.getSeconds() < 60) {
            time = duration.getSeconds() + " seconds ago";
        } else if (duration.toMinutes() < 60) {
            time = duration.toMinutes() + " minutes ago";
        } else if (duration.toHours() < 48) {
            time = duration.toHours() + " hours ago";
        } else {
            time = duration.toDays() + " days ago";
        }
        return time;
    }
}
