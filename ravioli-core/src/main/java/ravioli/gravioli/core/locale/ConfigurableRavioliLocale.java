package ravioli.gravioli.core.locale;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.api.RavioliModule;
import ravioli.gravioli.common.locale.RavioliLocale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigurableRavioliLocale implements RavioliLocale<CommandSender> {
    private final RavioliModule<CommandSender> module;
    private final Map<String, List<String>> messages = new HashMap<>();

    public ConfigurableRavioliLocale(@NotNull final RavioliModule<CommandSender> module) {
        this.module = module;
    }

    @Override
    public void send(@NotNull final CommandSender commandSender, @NotNull final String key,
                     @NotNull final TagResolver... placeholders) {
        final List<String> lines = this.messages.get(key);

        if (lines == null) {
            return;
        }
        lines.forEach((line) -> {
            final Component component = MiniMessage.miniMessage().deserialize(line, placeholders);

            commandSender.sendMessage(component);
        });
    }

    @Override
    public @NotNull List<Component> get(@NotNull final String key, @NotNull final TagResolver... placeholders) {
        final List<String> lines = this.messages.get(key);

        if (lines == null) {
            return Collections.emptyList();
        }
        return lines.stream()
                .map((line) -> MiniMessage.miniMessage().deserialize(line, placeholders))
                .toList();
    }

    @Override
    @Blocking
    public void reload() {
        final FileConfiguration configFile;

        try {
            configFile = this.load();
        } catch (final IllegalArgumentException e) {
            return;
        }
        this.messages.clear();

        if (configFile == null) {
            return;
        }
        this.populateAndTraverse("", configFile);
    }

    private void populateAndTraverse(@NotNull final String originalPath,
                                     @NotNull final ConfigurationSection currentSection) {
        final String currentPath = originalPath.isBlank() ? "" : originalPath + ".";

        for (final String key : currentSection.getKeys(false)) {
            final String path = currentPath + key;

            if (currentSection.isConfigurationSection(key)) {
                this.populateAndTraverse(path, Objects.requireNonNull(currentSection.getConfigurationSection(key)));

                continue;
            }
            List<String> values = new ArrayList<>();

            if (currentSection.isList(key)) {
                values = currentSection.getStringList(key);
            } else if (currentSection.isString(key)) {
                values = List.of(currentSection.getString(key, ""));
            }
            if (values.isEmpty()) {
                continue;
            }
            this.messages.put(
                    path,
                    ImmutableList.copyOf(values)
            );
        }
    }

    private @Nullable FileConfiguration load() {
        final File file = this.module.loadResource("locale.yml");

        if (!file.exists()) {
            return null;
        }
        final FileConfiguration configFile = YamlConfiguration.loadConfiguration(file);

        try (final InputStream inputStream = this.module.getClass().getResourceAsStream("/locale.yml");
             final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8); ) {
            final FileConfiguration latestLocaleConfig = YamlConfiguration.loadConfiguration(inputStreamReader);
            final boolean unmodifiedConfig = !this.traverse(configFile, latestLocaleConfig);

            if (unmodifiedConfig) {
                return configFile;
            }
            configFile.save(file);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return configFile;
    }

    private boolean traverse(@NotNull final FileConfiguration configFile, @NotNull final ConfigurationSection currentSection) {
        String parentPath = Objects.requireNonNullElse(currentSection.getCurrentPath(), "");

        if (!parentPath.isBlank()) {
            parentPath += ".";
        }
        boolean modifiedConfig = false;

        for (final String key : currentSection.getKeys(false)) {
            final String path = parentPath + key;

            if (currentSection.isConfigurationSection(key)) {
                final boolean modified = this.traverse(configFile, Objects.requireNonNull(currentSection.getConfigurationSection(key)));

                if (!modifiedConfig && modified) {
                    modifiedConfig = true;
                }
                continue;
            }
            if (configFile.contains(path)) {
                continue;
            }
            configFile.set(path, currentSection.get(key));

            modifiedConfig = true;
        }
        return modifiedConfig;
    }
}
