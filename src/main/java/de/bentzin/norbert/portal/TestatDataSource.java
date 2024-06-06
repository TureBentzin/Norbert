package de.bentzin.norbert.portal;

import de.bentzin.norbert.*;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface TestatDataSource extends Closeable {

    @NotNull
    Logger DATA_SOURCE_LOGGER = org.slf4j.LoggerFactory.getLogger(TestatDataSource.class);

    @NotNull
    default Logger getLogger() {
        Logger l = org.slf4j.LoggerFactory.getLogger(this.getClass());
        // l saved for later logger config
        return l;
    }

    record OverviewReturn(@NotNull List<Overview> overviews, @NotNull String sessionToken) {
    }

    void connect(final @NotNull URL url);

    /**
     * Get the overview for a given matriculation number with a new session token
     *
     * @return all overviews
     * @throws IllegalArgumentException if the matriculation number is invalid (Account will be suspended)
     * @throws IOException              if communication with the server fails
     */
    @NotNull
    OverviewReturn getOverviewFor(@NotNull Account account) throws IllegalArgumentException, IOException;

    /**
     * Get the overview for a given matriculation number with a given session token
     *
     * @return all overviews
     * @throws IllegalArgumentException if the matriculation number is invalid (Account will be suspended)
     * @throws IOException              if communication with the server fails
     */
    @NotNull
    List<Overview> getOverviewFor(@NotNull Account account, @NotNull String sessionToken) throws IllegalArgumentException, IOException;

}
