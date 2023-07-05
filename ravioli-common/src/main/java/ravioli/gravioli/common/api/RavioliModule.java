package ravioli.gravioli.common.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.file.FileConfiguration;
import ravioli.gravioli.common.locale.RavioliLocale;
import ravioli.gravioli.common.postgres.PostgresProvider;
import ravioli.gravioli.common.redis.RedisProvider;

import java.io.File;
import java.io.InputStream;

public interface RavioliModule<T> {
    /**
     * Load a plugin resource. If the resource is not found in the plugin's data directory, it will not be created
     * and will return a {@link File} object that does not exist.
     *
     * @param resourcePath the path to a given resource
     */
    default @NotNull File loadResource(@NotNull final String resourcePath) {
        return this.loadResource(resourcePath, true);
    }

    /**
     * Load a plugin resource. If the resource is not found in the plugin's data directory, it will be created
     * depending on the value of {@code save}.
     *
     * @param resourcePath the path to a given resource
     * @param save         if the resource is not found in the plugin's data directory, true to save it
     *                     to file or false to ignore it
     */
    default @NotNull File loadResource(@NotNull final String resourcePath, final boolean save) {
        final File file = new File(this.getDataFolder(), resourcePath);

        if (!file.exists() && save) {
            this.saveResource(resourcePath, true);
        }
        return file;
    }

    /**
     * Returns the folder that the module data's files are located in. The
     * folder may not yet exist.
     *
     * @return The root data folder
     */
    @NotNull File getDataFolder();

    /**
     * Returns the module-specific configuration file. The implementation of this configuration is completely
     * platform-dependent.
     *
     * @return  The modules config file
     */
    @NotNull FileConfiguration getModuleConfig();

    /**
     * Returns the locale for the module.
     *
     * @return The module locale
     */
    @NotNull RavioliLocale<T> getLocale();

    /**
     * Saves the raw contents of any resource embedded with a plugin's .jar
     * file assuming it can be found using {@link #getResource(String)}.
     * <p>
     * The resource is saved into the plugin's data folder using the same
     * hierarchy as the .jar file (subdirectories are preserved).
     *
     * @param resourcePath the embedded resource path to look for within the
     *     plugin's .jar file. (No preceding slash).
     * @param replace if true, the embedded resource will overwrite the
     *     contents of an existing file.
     * @throws IllegalArgumentException if the resource path is null, empty,
     *     or points to a nonexistent resource.
     */
    void saveResource(@NotNull String resourcePath, boolean replace);

    /**
     * Gets an embedded resource in this plugin
     *
     * @param fileName Filename of the resource
     * @return File if found, otherwise null
     */
    @Nullable InputStream getResource(@NotNull String fileName);

    @NotNull RedisProvider redisProvider();

    @NotNull PostgresProvider postgresProvider();
}
