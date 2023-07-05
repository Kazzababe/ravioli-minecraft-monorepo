package ravioli.gravioli.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class ItemUtil {
    /**
     * Parses and returns an {@link ItemStack} as specified by a common format of a {@link ConfigurationSection}.
     *
     * @param rootSection The root ConfigurationSection of the ItemStack.
     * @return An ItemStack
     * @throws IllegalArgumentException when an invalid configuration is found such as
     *                                  a missing material or invalid enum value being supplied.
     */
    public static @NotNull ItemStack parseItemStackFromConfig(@NotNull final ConfigurationSection rootSection) {
        final String materialValue = rootSection.getString("material");

        if (materialValue == null) {
            throw new IllegalArgumentException("No \"material\" field found in item configuration rootSection.");
        }
        final int amount = rootSection.getInt("amount", 1);
        final ItemBuilder itemBuilder = new ItemBuilder(Material.valueOf(materialValue.toUpperCase()), amount);
        final ConfigurationSection itemMetaSection = rootSection.getConfigurationSection("itemMeta");

        if (itemMetaSection != null) {
            if (itemMetaSection.contains("customModelData")) {
                itemBuilder.customModelData(itemMetaSection.getInt("customModelData"));
            }
            if (itemMetaSection.contains("unbreakable")) {
                itemBuilder.unbreakable(itemMetaSection.getBoolean("unbreakable"));
            }
            final ConfigurationSection enchantmentSection = itemMetaSection.getConfigurationSection("enchantments");

            if (enchantmentSection != null) {
                for (final String enchantmentName : enchantmentSection.getKeys(false)) {
                    final Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentName));

                    if (enchantment == null) {
                        continue;
                    }
                    itemBuilder.addEnchantment(enchantment, enchantmentSection.getInt(enchantmentName));
                }
            }
            final MiniMessage miniMessage = MiniMessage.miniMessage();

            Optional.ofNullable(itemMetaSection.getString("displayName"))
                    .ifPresent((displayName) -> itemBuilder.displayName(miniMessage.deserialize(displayName)));

            itemMetaSection.getStringList("lore")
                    .forEach((line) -> itemBuilder.addLore(miniMessage.deserialize(line)));
            itemMetaSection.getStringList("itemFlags")
                    .forEach((itemFlag) -> itemBuilder.addItemFlags(ItemFlag.valueOf(itemFlag.toUpperCase())));
        }
        return itemBuilder.build();
    }

    public static @NotNull String serializeItemStack(@NotNull final ItemStack itemStack) {
        try (
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)
        ) {
            dataOutput.writeObject(itemStack);

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to serialize ItemStack.", e);
        }
    }

    public static @NotNull ItemStack deserializeItemStack(@NotNull final String data) throws IOException {
        try (
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)
        ) {
            return (ItemStack) dataInput.readObject();
        } catch (final ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static @NotNull Component getItemStackName(@NotNull final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            return Component.translatable(itemStack.getType().translationKey());
        }
        return Objects.requireNonNull(itemMeta.displayName());
    }
}
