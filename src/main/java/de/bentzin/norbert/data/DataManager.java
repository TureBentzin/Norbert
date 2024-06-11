package de.bentzin.norbert.data;

import de.bentzin.norbert.Account;
import de.bentzin.norbert.Bot;
import de.bentzin.norbert.Overview;
import de.bentzin.norbert.Task;
import de.bentzin.norbert.portal.TestatDataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;


public class DataManager {
    private final @NotNull Logger logger = LoggerFactory.getLogger(this.getClass());
    private final @NotNull Supplier<TestatDataSource> testatDataSourceSupplier;
    private @Nullable TestatDataSource testatDataSource = null;

    private void fresh() {
        discard();
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

    private @NotNull TestatDataSource data() {
        if(testatDataSource == null) {
            fresh();
        }
        return testatDataSource;
    }

    private void discard() {
        if(testatDataSource != null) {
            logger.info("Closing data source");
            try {
                testatDataSource.close();
                logger.info("Closed data source successfully");
            } catch (IOException e) {
                logger.error("Failed to close data source", e);
                System.exit(Bot.RESTART_ERROR);
            }
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
        final List<Account> accounts = Bot.getDatabaseManager().getAccounts();

        for (Account account : accounts) {
            try {
                TestatDataSource.OverviewReturn overviews = data().getOverviewFor(account);
                for (Overview overview : overviews.overviews()) {
                    final List<Task> delta = Bot.getDatabaseManager().reportData(account.matr_nr(), overview);
                    for (Task task : delta) {
                        Guild guild = Bot.getJda().getGuildById(Bot.getConfig().getGuildId());
                        if(guild == null) {
                            logger.error("Failed to get the guild {} from the JDA? Is the Bot on it?", Bot.getConfig().getGuildId());
                            List<Guild> guilds = Bot.getJda().getGuilds();
                            logger.warn("Supported guilds are: {}", guilds.stream().map(Guild::getId).toList().toString());
                            System.exit(Bot.UNRECOVERABLE_ERROR);
                        }
                        TextChannel channel = guild.getTextChannelById(Bot.getConfig().getChannelId());
                        if(channel == null) {
                            logger.error("Failed to get the channel {} from the JDA? Was it deleted?", Bot.getConfig().getChannelId());
                            System.exit(Bot.UNRECOVERABLE_ERROR);
                        }
                        channel.sendMessage("New task: " + task + " of " + account.displayName() + "(" + account.matr_nr() + ") [Completed : " + (task.done() ? ":white_check_mark:" : ":x:") + "]").queue();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
