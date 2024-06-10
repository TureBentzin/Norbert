package de.bentzin.norbert;

import de.bentzin.norbert.portal.TestatDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
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
        try(Connection connection = connect()) {
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            logger.info("Enabled foreign keys in database!");
        }catch (SQLException e) {
            logger.error("Error while setting up database!", e);
            System.exit(Bot.UNRECOVERABLE_ERROR);
        }
    }

    public void createTables() {
        try (Connection connection = connect()) {
            logger.info("Creating tables in database...");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS accounts (matr_nr INTEGER PRIMARY KEY, displayname varchar(255), did varchar(255))");
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS modules (identifier varchar(255) PRIMARY KEY)");
            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS tasks
                    (
                        task_id   varchar(255) not null,
                        module_id varchar(255) not null
                            constraint tasks_modules_identifier_fk
                                references modules
                                on update restrict on delete restrict,
                        constraint tasks_pk
                            primary key (task_id, module_id)
                    )
                    """);
            connection.createStatement().execute("""
                    CREATE TABLE seassions
                    (
                        matr_nr  integer
                            constraint seassions_pk
                                primary key
                            constraint seassions_accounts_matr_nr_fk
                                references accounts
                                on update restrict on delete restrict,
                        seassion TEXT not null
                            constraint seassion_uk
                                unique
                    )
                    """);
            connection.createStatement().execute("""
                    create table data
                    (
                        matr_nr   integer               not null
                            constraint data_accounts_matr_nr_fk
                                references accounts
                                on update restrict on delete restrict,
                        task_id   varchar(255)          not null,
                        module_id varchar(255)          not null,
                        done      boolean default false not null,
                        constraint data_pk
                            primary key (matr_nr, module_id, task_id),
                        constraint data_tasks_module_id_task_id_fk
                            foreign key (module_id, task_id) references tasks (module_id, task_id)
                                on update restrict on delete restrict
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

    public @NotNull List reportData(@NotNull int matr_nr, @NotNull Overview overview) {
        try (Connection connection = connect()) {

        } catch (SQLException e) {
            logger.error("Error while reporting data to database!", e);
            System.exit(Bot.RESTART_ERROR);
        }
        return Collections.emptyList();
    }

}
