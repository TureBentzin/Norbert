package de.bentzin.norbert.data;

import de.bentzin.norbert.Bot;
import de.bentzin.norbert.portal.TestatDataSource;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;


public class DataManager {
    private final @NotNull Logger logger = LoggerFactory.getLogger(this.getClass());
    private final @NotNull Supplier<TestatDataSource> testatDataSourceSupplier;
    private @Nullable TestatDataSource testatDataSource = null;

    private void fresh() {
        if(testatDataSource != null) {
            logger.info("Closing old data source");
            try {
                testatDataSource.close();
                logger.info("Closed old data source successfully");
            } catch (IOException e) {
                logger.error("Failed to close old data source", e);
                System.exit(Bot.RESTART_ERROR);
            }
        }
        logger.info("Creating new data source");
        testatDataSource = testatDataSourceSupplier.get();
        logger.info("Created new data source successfully");
        logger.info("Passing the target to the datasource...");
        if(testatDataSource == null) {
            logger.error("Failed to create a new data source (null)");
            System.exit(Bot.UNRECOVERABLE_ERROR + 1);
        }
        try {
            testatDataSource.connect(new URL(Bot.getConfig().getUrl()));
        } catch (MalformedURLException e) {
           logger.error("Failed to parse the URL from the config to a valid URL", e);
              System.exit(Bot.UNRECOVERABLE_ERROR);
        }
    }

    public DataManager(@NotNull Supplier<TestatDataSource> dataSource) {
        this.testatDataSourceSupplier = dataSource;
        fresh();
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
