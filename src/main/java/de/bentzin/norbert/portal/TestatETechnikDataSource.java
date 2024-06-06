package de.bentzin.norbert.portal;

import de.bentzin.norbert.Account;
import de.bentzin.norbert.Overview;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Represents a data source for ta testat.etchnik.fh-aachen.de instance
 */
public class TestatETechnikDataSource  implements TestatDataSource {
    @Override
    public void connect(@NotNull URL url) {
        //TODO url is the url to connect to
    }

   /*

    */

    @Override
    public @NotNull OverviewReturn getOverviewFor(@NotNull Account account) throws IllegalArgumentException, IOException {
        //TODO get the overview for a given matriculation number with a new session token and return both
        return null;
    }

    @Override
    public @NotNull List<Overview> getOverviewFor(@NotNull Account account, @NotNull String sessionToken) throws IllegalArgumentException, IOException {
        //TODO get the overview for a given matriculation number with a given session token.
        return List.of();
    }

    @Override
    public void close() throws IOException {
        //TODO may be called at any time in program execution
        //Close all connections
    }
}
