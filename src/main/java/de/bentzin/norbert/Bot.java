package de.bentzin.norbert;

import de.bentzin.norbert.command.*;
import de.bentzin.norbert.data.DataManager;
import de.bentzin.norbert.portal.TestatDataSource;
import de.bentzin.norbert.UpdateTask;
import de.bentzin.norbert.command.GCommandListener;
import de.bentzin.norbert.portal.TestatETechnikDataSource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * @author Ture Bentzin
 * @since 07-10-2023
 */
public class Bot {

    /* Exit codes */
    public static final int EXIT_CODE_UNKOWN = -1;
    public static final int NORMAL_EXIT = 0; // Everything is fine no restart
    public static final int RESTART_NO_UPDATE = 1; // Restart without updating
    public static final int RESTART_UPDATE = 2; // Restart with updating
    public static final int RESTART_ERROR = 3; // Restart because of error (try to update)
    public static final int UNRECOVERABLE_ERROR = 4; // Unrecoverable error (will not fix itself)
    /* Exit codes */

    @NotNull
    public static final Logger logger = LoggerFactory.getLogger(Bot.class);
    /* Managers (populated here or before bot start) */
    @NotNull
    private static final GsonManager gsonManager = new GsonManager();
    private static final long session_start = System.currentTimeMillis();
    /* DEBUG */
    public static boolean debug = false;
    @Nullable
    private static DatabaseManager databaseManager;
    /* Objects populated on main */
    @Nullable
    private static JDA jda;
    @Nullable
    private static ConfigObject configObject;

    @Nullable
    private static DataManager dataManager;

    @Nullable
    private static Thread updateThread;

    @Nullable
    private static Integer shutdown = null;

    /**
     * Entrypoint of application
     *
     * @param args 0 = token
     *             1 = bot_config [file]
     *             [2] = -d (debug)
     */
    public static void main(String[] args) {

        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        String token = null;
        File config;
        GCommandListener gCommandListener;
        try {
            //https://www.slf4j.org/api/org/slf4j/simple/SimpleLogger.html

            if (args.length > 2 && args[2].equals("-d")) {
                debug = true;
                root.setLevel(Level.FINER);
                root.getHandlers()[0].setLevel(Level.FINER);
                logger.info("Debug mode enabled!");
            }
            if (args.length < 1 || args[0].isEmpty()) {
                logger.error("Please provide token at args[0]!");
                System.exit(UNRECOVERABLE_ERROR);
            } else {
                token = args[0];
                logger.info("Token was loaded from argument!");
            }
            if (args.length < 2 || args[1].isEmpty()) {
                logger.error("Please provide bot_config at args[1]!");
                System.exit(UNRECOVERABLE_ERROR);
            } else {
                config = new File(args[1]);
                if (!config.exists()) {
                    logger.error("{} does not exist!", config.getAbsolutePath());
                    createConfig(config);
                    logger.info("Please fill out the config file at {}. The application will shut down now!", config.getAbsolutePath());
                    System.exit(UNRECOVERABLE_ERROR);
                } else {
                    //config exists
                    configObject = getGsonManager().fromJson(config, ConfigObject.class);
                    if (configObject == null) {
                        logger.error("Could not load config file!");
                        System.exit(UNRECOVERABLE_ERROR);
                    } else {

                        logger.info("Config file was loaded successfully!");
                    }
                }
            }

            // continue bootstrap
            databaseManager = new DatabaseManager(configObject.getSqlitePath());
            logger.info("DatabaseManager was created successfully!");
            {
                //initial setup of the database
                databaseManager.setup();
                databaseManager.createTables();
            }

            dataManager = new DataManager(TestatETechnikDataSource::new);
            logger.info("DataManager was created successfully!");

            /* Commands */
            gCommandListener = new GCommandListener();

            SayCommand sayCommand = new SayCommand();
            gCommandListener.register(sayCommand);
            gCommandListener.register(new ExitCommand());
            gCommandListener.register(new UpdateCommand());
            gCommandListener.register(new SignupCommand());
            gCommandListener.register(new OverviewCommand());
            if (debug) gCommandListener.register(new ConnectTestCommand());

        } catch (Exception e) {
            logger.error("Error while starting bot!", e);
            System.exit(UNRECOVERABLE_ERROR); //skipping shutdown because it is not initialized
            return;
        }
        //JDA Startup
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(token);
            jdaBuilder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
            jdaBuilder.setBulkDeleteSplittingEnabled(false).setAutoReconnect(true);
            jdaBuilder.addEventListeners(gCommandListener);
            jda = jdaBuilder.build();
            gCommandListener.updateJDA(jda);
        } catch (Exception e) {
            logger.error("Error while starting JDA! Restarting...", e);
            System.exit(RESTART_ERROR);
        }

        if (configObject.isWriteEnabled()) {
            logger.info("Executing initial UpdateTask procedure!");
            updateThread = UpdateTask.execute();
        }
        logger.info("Bot started successfully!");
        //await shutdown
        while (shutdown == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("Main thread was interrupted!", e);
            }
        }
        shutdown(shutdown);

    }

    @NotNull
    public static JDA getJda() {
        return jda;
    }

    public static void createConfig(@NotNull File file) {
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        try {
            boolean newFile = file.createNewFile();
            if (newFile) {
                logger.info("Created new config file at {}", file.getAbsolutePath());
                //populate config
                ConfigObject configObject = ConfigObject.defaultConfig();
                getGsonManager().toJson(file, configObject);
                logger.info("Populated new config file with default values!");
            } else {
                logger.error("Could not create new config file at {}", file.getAbsolutePath());
                System.exit(UNRECOVERABLE_ERROR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    @Nullable
    public static ConfigObject getConfig() {
        return configObject;
    }

    @NotNull
    public static GsonManager getGsonManager() {
        return gsonManager;
    }

    public static boolean isAdmin(long id) {
        return configObject.getAdminIds().contains(id);
    }

    public static long getSessionDuration() {
        return System.currentTimeMillis() - session_start;
    }

    @NotNull
    public static String getSessionDurationString() {
        return Utils.formatDuration(Duration.ofMillis(getSessionDuration()));
    }

    @NotNull
    public static String getSessionStartTimeFormat() {
        return TimeFormat.RELATIVE.format(session_start);
    }


    public static void shutdownAsync() {
        shutdown = NORMAL_EXIT;
    }

    public static void shutdownAsync(int code) {
        shutdown = code;
    }

    public static void shutdown() {
        shutdown(NORMAL_EXIT);
    }

    public static void shutdown(int code) throws IllegalStateException {
        //check if called from main thread
        if (!Thread.currentThread().getName().equals("main")) {
            throw new IllegalStateException("Shutdown was not called from main thread!");
        }
        logger.info("Shutting down with exit code {} after session with length of {}", code, getSessionDurationString());
        logger.info("Stopping UpdateThread!");
        if (updateThread != null) {
            updateThread.interrupt();
        }

        if (jda != null) {
            logger.info("Shutting down JDA!");
            jda.shutdownNow();

            try {
                boolean timeout = jda.awaitShutdown(Duration.ofMinutes(5));
                if (!timeout) {
                    logger.warn("JDA did not shutdown in time!");
                }
            } catch (InterruptedException e) {
                logger.error("Error while waiting for JDA to shutdown!", e);
            }


        } else {
            logger.warn("JDA is null. Cannot shutdown JDA!");
        }

        System.exit(code);
        throw new IllegalStateException("JVM should be already exited!");
    }


    public static <R> @Nullable R accessJDA(@NotNull Function<JDA, R> action, @Nullable R fallback) {
        if (jda == null) {
            final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
            final StackWalker.StackFrame frame = walker.walk(stackFrameStream -> stackFrameStream.findFirst().orElse(null));
            final String frameInfo = frame == null ? "Unknown" : frame.getClassName() + "#" + frame.getMethodName();
            logger.warn("JDA is not present (null). {} attempted to access but failed!", frameInfo);
        } else {
            try {
                return action.apply(jda);
            } catch (Exception e) {
                logger.error("Error while accessing JDA!", e);
                logger.error("Bot will exit and restart!");
                shutdown(RESTART_ERROR);
            }
        }
        return fallback;
    }


    @Nullable
    public static DataManager getDataManager() {
        return dataManager;
    }
}