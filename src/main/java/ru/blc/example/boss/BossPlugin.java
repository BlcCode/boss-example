package ru.blc.example.boss;

import com.google.common.base.Charsets;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class BossPlugin extends JavaPlugin {

    private File messagesFile;
    private FileConfiguration messagesConfiguration;
    @Getter
    private SqlConnection sqlConnection;

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
        reloadConfig();
    }

    @Override
    public void onEnable() {
        if (!createDatabaseConnection()) {
            //creation error logs by SqlConnection.class
            getSLF4JLogger().error("Plugin will disabled!");
            setEnabled(false);
            return;
        }
    }

    @Override
    public void onDisable() {
        if (sqlConnection != null) {
            sqlConnection.close();
        }
    }

    //other

    public FileConfiguration getMessagesConfiguration() {
        if (messagesConfiguration == null) reloadConfig();
        return messagesConfiguration;
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
