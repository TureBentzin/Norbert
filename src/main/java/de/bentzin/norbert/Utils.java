package de.bentzin.norbert;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class Utils {

    @NotNull
    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

        // Build formatted string
        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");

        return builder.toString();
    }
}
