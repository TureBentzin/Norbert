package de.bentzin.norbert;

import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ture Bentzin
 * @since 29-03-2024
 */
public class UpdateScheduler implements Runnable {

    @NotNull
    public static final Logger logger = LoggerFactory.getLogger(UpdateScheduler.class);
    public static final int MINUTES_INTERVALL = 10;
    private static int failedAttempts = 0;

    @NotNull
    public static Thread execute() {
        Thread thread = new Thread(new UpdateScheduler(), "UpdateThread");
        logger.info("Starting Update scheduler thread.");
        thread.start();
        return thread;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000); //Sleep for api...
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long lastUpdateTimestamp = 0L;
        while (true) {
            //Hourly basic update
            if(lastUpdateTimestamp + (60 * 60 * 1000) > System.currentTimeMillis()){
                logger.info("Running update task!");
                if (Bot.getDataManager() != null) {
                    Bot.getDataManager().update();
                    logger.info("Updated executed!");
                    failedAttempts = 0;
                    lastUpdateTimestamp = System.currentTimeMillis();
                } else {
                    logger.error("DataManager is null. Cannot update.");
                    failedAttempts++;
                }
            }

            //time specific module Updates
            logger.info("Scheduling time specific update tasks!");
            if (Bot.getDataManager() != null) {
                Bot.getDataManager().scheduleUpdateTasks();
                logger.info("Tasks Scheduled");
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
