package ravioli.gravioli.customitem.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.customitem.config.item.VanillaItemDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustomItemConfiguration {
    private final Map<Material, VanillaItemDetails> vanillaItemDetails = new HashMap<>();

    public void load(@NotNull final FileConfiguration configFile) {
        final ConfigurationSection rootSection = configFile.getConfigurationSection("vanilla-items");

        if (rootSection == null) {
            return;
        }
        for (final String key : rootSection.getKeys(false)) {
            final Material material;

            try {
                material = Material.valueOf(key.toUpperCase());
            } catch (final IllegalArgumentException e) {
                continue;
            }
            this.vanillaItemDetails.put(
                    material,
                    new VanillaItemDetails(
                            rootSection.getString(key + ".displayName"),
                            rootSection.getStringList(key + ".lore")
                    )
            );
        }
    }

    public @NotNull Optional<@NotNull VanillaItemDetails> getDetails(@NotNull final Material material) {
        return Optional.ofNullable(this.vanillaItemDetails.get(material));
    }
}
