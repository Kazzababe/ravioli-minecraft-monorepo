package ravioli.gravioli.core.api;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;
import ravioli.gravioli.common.api.RavioliModule;
import ravioli.gravioli.common.locale.RavioliLocale;
import ravioli.gravioli.common.postgres.BasicPostgresProvider;
import ravioli.gravioli.common.postgres.PostgresProvider;
import ravioli.gravioli.common.redis.BasicRedisProvider;
import ravioli.gravioli.common.redis.RedisProvider;
import ravioli.gravioli.core.locale.ConfigurableRavioliLocale;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class RavioliPlugin extends JavaPlugin implements RavioliModule<CommandSender> {
    private final RavioliLocale<CommandSender> locale;

    private RedisProvider redisProvider;
    private PostgresProvider postgresProvider;
    private org.simpleyaml.configuration.file.FileConfiguration moduleConfig;

    public RavioliPlugin() {
        this.locale = new ConfigurableRavioliLocale(this);
    }

    @Override
    public final void onLoad() {
        this.locale.reload();

        this.onPluginLoad();
    }

    @Override
    public final void onEnable() {
        this.onPluginEnable();
    }

    @Override
    public final void onDisable() {
        this.onPluginDisable();

        if (this.postgresProvider != null) {
            this.postgresProvider.close();
        }
        if (this.redisProvider != null) {
            this.redisProvider.close();
        }
    }

    @Override
    public org.simpleyaml.configuration.file.@NotNull FileConfiguration getModuleConfig() {
        if (this.moduleConfig == null) {
            this.reloadModuleConfig();
        }
        return Objects.requireNonNull(this.moduleConfig);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        this.reloadModuleConfig();
    }

    @Override
    public @NotNull RavioliLocale<CommandSender> getLocale() {
        return this.locale;
    }

    protected void onPluginLoad() {}

    protected void onPluginEnable() {}

    protected void onPluginDisable() {}

    @Override
    public final @NotNull RedisProvider redisProvider() {
        if (this.redisProvider == null) {
            this.redisProvider = this.createRedisProvider();
        }
        return this.redisProvider;
    }

    @Override
    public final @NotNull PostgresProvider postgresProvider() {
        if (this.postgresProvider == null) {
            this.postgresProvider = this.createPostgresProvider();
        }
        return this.postgresProvider;
    }

    private @NotNull RedisProvider createRedisProvider() {
        final File databaseFile = this.loadResource("database.yml");

        if (!databaseFile.exists()) {
            throw new RuntimeException("Unable to create redis service as no database.yml file was found.");
        }
        final FileConfiguration config = YamlConfiguration.loadConfiguration(databaseFile);
        final ConfigurationSection dataSourcesSection = config.getConfigurationSection("data-sources");

        if (dataSourcesSection == null) {
            throw new RuntimeException("Unable to create redis service as the database.yml file does not contain a data-sources section.");
        }
        final ConfigurationSection redisSection = dataSourcesSection.getConfigurationSection("redis");

        if (redisSection == null) {
            throw new RuntimeException("Unable to create redis service as the database.yml file does not contain a data-sources.redis section.");
        }
        final ConfigurationSection optionsSection = redisSection.getConfigurationSection("options");
        final Map<String, String> options = new HashMap<>();

        if (optionsSection != null) {
            for (final String key : optionsSection.getKeys(false)) {
                options.put(key, optionsSection.getString(key, ""));
            }
        }
        return new BasicRedisProvider(
            Objects.requireNonNull(
                redisSection.getString("host")
            ),
            redisSection.getInt("port", 6379),
            redisSection.getString("password"),
            options
        );
    }

    private @NotNull PostgresProvider createPostgresProvider() {
        final File databaseFile = this.loadResource("database.yml");

        if (!databaseFile.exists()) {
            throw new RuntimeException("Unable to create persistence provider as no database.yml file was found.");
        }
        final FileConfiguration config = YamlConfiguration.loadConfiguration(databaseFile);
        final ConfigurationSection dataSourcesSection = config.getConfigurationSection("data-sources");

        if (dataSourcesSection == null) {
            throw new RuntimeException("Unable to create persistence provider as the database.yml file does not contain a data-sources section.");
        }
        final ConfigurationSection postgresSection = dataSourcesSection.getConfigurationSection("postgres");

        if (postgresSection == null) {
            throw new RuntimeException("Unable to create persistence provider as the database.yml file does not contain a data-sources.mysql section.");
        }
        final ConfigurationSection optionsSection = postgresSection.getConfigurationSection("options");
        final Map<String, String> options = new HashMap<>();

        if (optionsSection != null) {
            for (final String key : optionsSection.getKeys(false)) {
                options.put(key, optionsSection.getString(key, ""));
            }
        }
        return new BasicPostgresProvider(
            Objects.requireNonNull(
                postgresSection.getString("host")
            ),
            postgresSection.getInt("port", 3306),
            postgresSection.getString("username"),
            postgresSection.getString("password"),
            Objects.requireNonNull(
                postgresSection.getString("database")
            ),
            options
        );
    }

    private void reloadModuleConfig() {
        try {
            this.moduleConfig = YamlFile.loadConfigurationFromString(this.getConfig().saveToString());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
