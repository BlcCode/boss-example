package ru.blc.example.boss;

import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class SqlConnection {

    public static final String createTableQuery = """
            CREATE TABLE IF NOT EXISTS `defating` (
            \t`boss` TINYINT(4) NOT NULL,
            \t`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            \t`best_players` TEXT NOT NULL
            )
            ENGINE=InnoDB;""";

    private final JavaPlugin plugin;
    private Connection connection;

    public SqlConnection(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Create connection to database with specified params
     *
     * @return true if connection successful, otherwise false
     */
    public boolean connect(@NotNull String host, String port, @NotNull String username, @NotNull String password, @NotNull String databaseName) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true", username, password);
            return true;
        } catch (SQLException ex) {
            plugin.getSLF4JLogger().error("Connection to database failed!", ex);
            return false;
        }
    }

    public void close() {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException throwables) {
            plugin.getSLF4JLogger().error("Disconnection from database failed!", throwables);
        }
    }

    /**
     * Creates statement
     *
     * @param sql  sql expression
     * @param args arguments
     * @return {@link Optional} with prepared statement, or empty if there is errors while statement creating
     */
    public @NotNull Optional<PreparedStatement> prepareStatement(String sql, Object... args) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
            return Optional.of(statement);
        } catch (SQLException throwables) {
            plugin.getSLF4JLogger().error("Failed to prepare statement!", throwables);
            return Optional.empty();
        }
    }

    /**
     * Executes specified statement and close it
     *
     * @param statement statement for execution
     */
    @Contract(mutates = "param1")
    public void executeUpdate(PreparedStatement statement) {
        try {
            statement.executeUpdate();
            statement.close();
        } catch (SQLException throwables) {
            plugin.getSLF4JLogger().error("Query execution failed!", throwables);
        }
    }

    /**
     * Runs {@link SqlConnection#executeUpdate(PreparedStatement)} at async task
     */
    public void executeUpdateAsync(PreparedStatement statement) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> executeUpdate(statement));
    }

}
