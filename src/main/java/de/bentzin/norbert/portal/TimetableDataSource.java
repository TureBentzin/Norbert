package de.bentzin.norbert.portal;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimetableDataSource extends Closeable {
    @NotNull
    Logger DATA_SOURCE_LOGGER = org.slf4j.LoggerFactory.getLogger(de.bentzin.norbert.portal.TestatDataSource.class);

    @NotNull
    default Logger getLogger() {
        Logger l = org.slf4j.LoggerFactory.getLogger(this.getClass());
        // l saved for later logger config
        return l;
    }

    record dayEvent(@NotNull LocalDate date, @NotNull LocalTime start, @NotNull LocalTime end){}
    record timetableReturn(@NotNull List<dayEvent> overviews, @NotNull String modulCode) {}

    void connect(final @NotNull URL url);

    /**
     * Get the timetable for a given module code
     *
     * @return timetable object
     * @throws IllegalArgumentException if the modulCode number is invalid or unknown
     * @throws IOException              if communication with the server fails
     */
    @NotNull
    de.bentzin.norbert.portal.TimetableDataSource.timetableReturn getOverviewFor(@NotNull String modulCode) throws IllegalArgumentException, IOException;

    default boolean isClosed() {
        return false;
    }
}
