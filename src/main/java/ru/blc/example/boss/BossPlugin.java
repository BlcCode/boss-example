package ru.blc.example.boss;

import com.google.common.base.Charsets;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.blc.example.boss.api.boss.BossManager;
import ru.blc.example.boss.impl.boss.SimpleBossManager;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class BossPlugin extends JavaPlugin {

    private File messagesFile;
    private FileConfiguration messagesConfiguration;
    @Getter
    private SqlConnection sqlConnection;
    @Getter
    private BossManager bossManager;

    //bukkit methods
    @Override
    public void saveDefaultConfig() {
        if (!messagesFile.exists()) {
            this.saveResource("messages.yml", false);
        }
        super.saveDefaultConfig();
    }

    @Override
    public void reloadConfig() {
        this.messagesConfiguration = YamlConfiguration.loadConfiguration(this.messagesFile);
        InputStream defConfigStream = this.getResource("messages.yml");
        if (defConfigStream != null) {
            this.messagesConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
        }
        super.reloadConfig();
    }

    @Override
    public void saveConfig() {
        //nothing
    }

    //plugin logic
    @Override
    public void onLoad() {
        messagesFile = new File(getDataFolder(), "messages.yml");

        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        reloadConfig();
        if (!createDatabaseConnection()) {
            //creation error logs by SqlConnection.class
            getSLF4JLogger().error("Plugin will disabled!");
            setEnabled(false);
            return;
        }
        sqlConnection.prepareStatement(SqlConnection.createTableQuery).ifPresentOrElse(sqlConnection::executeUpdate,
                () -> {
                    throw new IllegalStateException();
                });
        bossManager = SimpleBossManager.create(this);
    }

    @Override
    public void onDisable() {
        if (sqlConnection != null) {
            sqlConnection.close();
        }
        if (bossManager != null) {
            bossManager.killAll();
        }
    }

    //other

    public FileConfiguration getMessagesConfiguration() {
        if (messagesConfiguration == null) reloadConfig();
        return messagesConfiguration;
    }

    //this translation methods is not optimised and should not be normally used
    public @NotNull String getTranslation(@NotNull String key, @NotNull Object @NotNull ... values) {
        String translation = getMessagesConfiguration().getString(key);
        if (translation == null) return "§cNo translation for key" + key;
        if (values.length == 0) return translation;
        return String.format(translation, values);
    }

    public @NotNull List<@NotNull String> getTranslationList(@NotNull String key, @NotNull Object @NotNull ... values) {
        var translation = getMessagesConfiguration().getStringList(key);
        if (translation.isEmpty()) return List.of("§cNo translation for key" + key);
        if (values.length == 0) return translation;
        return List.of(String.format(String.join("\n", translation), values).split("\n"));
    }

    public @NotNull String getTranslationListAsString(@NotNull String key, @NotNull Object @NotNull ... values) {
        var translation = getMessagesConfiguration().getStringList(key);
        if (translation.isEmpty()) return "§cNo translation for key" + key;
        if (values.length == 0) return String.join("\n", translation);
        return String.format(String.join("\n", translation), values);
    }

    //private
    private boolean createDatabaseConnection() {
        sqlConnection = new SqlConnection(this);
        //store database credentials at config is very dangerous, and requires configuring database for each plugin
        String host = Objects.requireNonNull(System.getenv("SQL_HOST"), "Specify SQL_HOST env");
        String port = Objects.requireNonNullElse(System.getenv("SQL_PORT"), "3306");
        String user = Objects.requireNonNull(System.getenv("SQL_USER"), "Specify SQL_USER env");
        String password = Objects.requireNonNull(System.getenv("SQL_PASSWORD"), "Specify SQL_PASSWORD env");
        String database = getConfig().getString("database", "");
        return !database.isBlank() && sqlConnection.connect(host, port, user, password, database);
    }
}
