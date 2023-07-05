package ravioli.gravioli.customitem.item.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.customitem.data.CustomItemData;
import ravioli.gravioli.customitem.enchantment.FakeGlowEnchantment;

import java.util.*;

public class VanillaCustomItemData implements CustomItemData {
    private final Set<ItemFlag> itemFlags;
    private final Component displayName;
    private final Map<Enchantment, Integer> enchantments;

    public VanillaCustomItemData(@NotNull final JsonObject data) {
        if (data.has("displayName")) {
            this.displayName = GsonComponentSerializer.gson().deserializeFromTree(data.get("displayName"));
        } else {
            this.displayName = null;
        }
        if (data.has("itemFlags")) {
            final JsonArray itemFlagsArray = data.get("itemFlags").getAsJsonArray();
            final Set<ItemFlag> parsedItemFlags = new HashSet<>();

            for (final JsonElement itemFlagElement : itemFlagsArray) {
                parsedItemFlags.add(ItemFlag.valueOf(itemFlagElement.getAsString()));
            }
            this.itemFlags = ImmutableSet.copyOf(parsedItemFlags);
        } else {
            this.itemFlags = Collections.emptySet();
        }
        if (data.has("enchantments")) {
            final JsonArray enchantmentsArray = data.get("enchantments").getAsJsonArray();
            final Map<Enchantment, Integer> parsedEnchantments = new HashMap<>();

            for (final JsonElement enchantmentElement : enchantmentsArray) {
                final JsonObject enchantmentJson = enchantmentElement.getAsJsonObject();
                final String enchantmentId = enchantmentJson.get("enchantment").getAsString();
                final Enchantment enchantment = enchantmentId.equals(FakeGlowEnchantment.GLOW_ID) ?
                        FakeGlowEnchantment.GLOW_ENCHANTMENT :
                        Enchantment.getByKey(NamespacedKey.minecraft(enchantmentId));

                if (enchantment == null) {
                    continue;
                }
                final int level = enchantmentJson.get("level").getAsInt();

                parsedEnchantments.put(enchantment, level);
            }
            this.enchantments = ImmutableMap.copyOf(parsedEnchantments);
        } else {
            this.enchantments = Collections.emptyMap();
        }
    }

    public VanillaCustomItemData(@NotNull final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            this.itemFlags = ImmutableSet.copyOf(itemMeta.getItemFlags());
            this.displayName = itemMeta.displayName();
            this.enchantments = ImmutableMap.copyOf(itemMeta.getEnchants());
        } else {
            this.itemFlags = Collections.emptySet();
            this.displayName = null;
            this.enchantments = Collections.emptyMap();
        }
    }

    public VanillaCustomItemData() {
        this.itemFlags = Collections.emptySet();
        this.displayName = null;
        this.enchantments = Collections.emptyMap();
    }

    public @Nullable Component displayName() {
        return this.displayName;
    }

    public @NotNull Map<Enchantment, Integer> enchantments() {
        return this.enchantments;
    }

    public @NotNull Set<ItemFlag> itemFlags() {
        return this.itemFlags;
    }

    @Override
    public @NotNull JsonObject data() {
        final JsonObject jsonObject = new JsonObject();

        if (this.displayName != null) {
            jsonObject.add(
                    "displayName",
                    GsonComponentSerializer.gson().serializeToTree(this.displayName)
            );
        }
        if (this.enchantments.size() > 0) {
            final JsonArray enchantmentsArray = new JsonArray();

            this.enchantments.forEach((enchantment, level) -> {
                final JsonObject data = new JsonObject();

                data.addProperty("enchantment", enchantment.getKey().getKey());
                data.addProperty("level", level);

                enchantmentsArray.add(data);
            });
            jsonObject.add("enchantments", enchantmentsArray);
        }
        if (this.itemFlags.size() > 0) {
            final JsonArray itemFlagsArray = new JsonArray();

            this.itemFlags.forEach((itemFlag) ->
                    itemFlagsArray.add(itemFlag.name())
            );
            jsonObject.add("itemFlags", itemFlagsArray);
        }
        return jsonObject;
    }
}
