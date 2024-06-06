package de.bentzin.norbert;

import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ture Bentzin
 * @since 29-03-2024
 */
public class UpdateTask implements Runnable {

    @NotNull
    public static final Logger logger = LoggerFactory.getLogger(UpdateTask.class);
    public static final int MINUTES_INTERVALL = 10;
    private static int failedAttempts = 0;

    @NotNull
    public static Thread execute() {
        Thread thread = new Thread(new UpdateTask(), "UpdateThread");
        logger.info("Starting UpdateTask thread.");
        thread.start();
        return thread;
    }

    @Override
    public void run() {
        while (true) {
            logger.info("Running update task!");
            if (Bot.getDataManager() != null) {
                Bot.getDataManager().update();
                logger.info("Updated executed!");
                failedAttempts = 0;
            } else {
                logger.error("DataManager is null. Cannot update.");
                failedAttempts++;
            }
            if (failedAttempts >= 3) {
                logger.error("Failed to execute update procedure 3 times in a row. Restarting bot.");
                logger.error("Restarting bot and updating the bot.");
                Bot.shutdown(Bot.RESTART_UPDATE);
            }
            try {
                Thread.sleep(10 * 60 * 1000);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted while sleeping. Exiting thread.");
                logger.debug(e.getMessage(), e);
                return;
            }
        }
    }
}
