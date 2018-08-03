package im.conversations.compliance.utils;

import java.time.Duration;
import java.time.Instant;

public class TimeUtils {
    /**
     * Get pretty formatted string for the time that has passed since a particular instant
     * @param then the time interval from which we need to get the difference
     * @return formatted string showing time elapsed
     */
    public static String getTimeSince(Instant then) {
        Instant now = Instant.now();
        Duration duration = Duration.between(then, now);
        String time;
        if (duration.getSeconds() < 5) {
            time = "just now";
        } else if (duration.getSeconds() < 60) {
            time = duration.getSeconds() + " seconds ago";
        } else if (duration.toMinutes() == 1) {
            time = "1 minute ago";
        } else if (duration.toMinutes() < 60) {
            time = duration.toMinutes() + " minutes ago";
        } else if (duration.toHours() == 1) {
            time = "1 hour ago";
        } else if (duration.toHours() < 48) {
            time = duration.toHours() + " hours ago";
        } else {
            time = duration.toDays() + " days ago";
        }
        return time;
    }
}
