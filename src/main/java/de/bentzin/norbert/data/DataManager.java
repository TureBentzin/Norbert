package de.bentzin.norbert.data;

import de.bentzin.norbert.portal.TestatDataSource;
import org.jetbrains.annotations.NotNull;

public class DataManager {
    private final @NotNull TestatDataSource dataSource;

    public DataManager(@NotNull TestatDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update() {
        //TODO
        /*
        1. Get all accounts from the database
        2. For each account:
            3. Get the overview from the dataSource
            4. Report the data to the database
            5. Announce the new data to the channel and mention the account
         */
    }

}
