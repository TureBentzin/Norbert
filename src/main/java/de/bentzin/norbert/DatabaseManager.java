package de.bentzin.norbert;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;


/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class DatabaseManager {

    @NotNull
    public static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    @NotNull
    private final String sqlitePath;

    @NotNull
    private final Set<Connection> connections = new HashSet<>();

    protected DatabaseManager(@NotNull String sqlitePath) {
        this.sqlitePath = sqlitePath;
    }

    public synchronized void close() {
        connections.forEach(connection -> {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        connections.clear();
    }

    @NotNull
    public synchronized Connection connect() {
        try {
            if (Bot.debug) logger.debug("Connecting to database: {}", sqlitePath);
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
            connections.add(connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Specialized methods for the database */

    public void setup() {
        try (Connection connection = connect()) {
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            logger.info("Enabled foreign keys in database!");
        } catch (SQLException e) {
            logger.error("Error while setting up database!", e);
            System.exit(Bot.UNRECOVERABLE_ERROR);
        }
    }

    public void createTables() {
        try (Connection connection = connect()) {
            logger.info("Creating tables in database...");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS accounts (matr_nr INTEGER PRIMARY KEY, displayname varchar(255), did varchar(255))");
            connection.createStatement().execute("""
                                            CREATE TABLE IF NOT EXISTS sessions (
                                                matr_nr INTEGER PRIMARY KEY
                                                                                constraint sessions_accounts_matr_nr_fk
                                                                                    references accounts on update restrict on delete restrict,
                                                                                session_token char(26) default NULL
                                            )
                    """);
            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS data
                    (
                        matr_nr   integer               not null
                            constraint data_accounts_matr_nr_fk
                                references accounts
                                on update restrict on delete restrict,
                        task_id   varchar(255)          not null,
                        module_id varchar(255)          not null,
                        done      boolean default false not null,
                        constraint data_pk
                            primary key (matr_nr, module_id, task_id)
                    );
                    """);


        } catch (SQLException e) {
            logger.error("Error while creating tables in database!", e);
            System.exit(Bot.UNRECOVERABLE_ERROR);
        }
    }

    public @NotNull List<Account> getAccounts() {
        try (Connection connection = connect()) {
            List<Account> accounts = new ArrayList<>();
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM accounts");
            while (resultSet.next()) {
                final Account data = new Account(resultSet.getString("displayname"),
                        resultSet.getLong("did"),
                        resultSet.getInt("matr_nr"));
                accounts.add(data);
            }
            return accounts;
        } catch (SQLException e) {
            logger.error("Error while getting accounts from database!", e);
            return Collections.emptyList();
        }
    }

    public @NotNull Optional<String> getSession(int matr_nr) {
        try (Connection connection = connect()) {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT session_token FROM seassions WHERE matr_nr = " + matr_nr);
            if (resultSet.next()) {
                return Optional.of(resultSet.getString("session_token"));
            }
        } catch (SQLException e) {
            logger.error("Error while getting session from database!", e);
        }
        return Optional.empty();
    }

    public @NotNull List<Task> getTasks(int matr_nr, @NotNull String module_id) {
        try (Connection connection = connect()) {
            List<Task> tasks = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM data WHERE matr_nr = ? AND module_id = ?");
            preparedStatement.setInt(1, matr_nr);
            preparedStatement.setString(2, module_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                final Task data = new Task(resultSet.getString("task_id"), resultSet.getBoolean("done"));
                tasks.add(data);
            }
            return tasks;
        } catch (SQLException e) {
            logger.error("Error while getting tasks from database!", e);
            return Collections.emptyList();
        }
    }

    public @NotNull List<Task> reportData(@NotNull int matr_nr, @NotNull Overview overview) {
        final List<Task> tasks = getTasks(matr_nr, overview.getIdentifier());
        final List<Task> delta = new ArrayList<>();
        for (Task task : overview.getTasks()) {
            if (!tasks.contains(task)) {
                logger.info("New task: {} of {} [Completed : {}]", task, matr_nr, task.done() ? "Yes" : "No");
                delta.add(task);
            }
        }
        logger.info("Reporting {} new tasks for account {}", delta.size(), matr_nr);
        try (Connection connection = connect()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO data (matr_nr, task_id, module_id, done) VALUES (?, ?, ?, ?)");
            for (Task task : delta) {
                preparedStatement.setInt(1, matr_nr);
                preparedStatement.setString(2, task.name());
                preparedStatement.setString(3, overview.getIdentifier());
                preparedStatement.setBoolean(4, task.done());
                preparedStatement.addBatch(); //does this work as expected?
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            logger.error("Error while reporting data to database!", e);
            System.exit(Bot.RESTART_ERROR);
        }
        return delta;
    }

}
