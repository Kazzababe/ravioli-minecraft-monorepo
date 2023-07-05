package ravioli.gravioli.customitem.service;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.customitem.data.CustomItemData;
import ravioli.gravioli.customitem.item.CustomItem;

import java.util.Optional;

public interface CustomItemService {
    @NotNull CustomItem<?> registerCustomItem(@NotNull CustomItem<?> customItem);

    @NotNull Optional<@NotNull CustomItem> getCustomItem(@NotNull String id);

    @NotNull <T extends CustomItem> Optional<@NotNull T> getCustomItem(@NotNull Class<T> customItemClass);

    @NotNull Optional<@NotNull CustomItem> getCustomItem(@NotNull ItemStack itemStack);

    @NotNull <T extends CustomItemData> ItemStack createItemStack(@NotNull CustomItem<T> customItem, @NotNull T data);

    default @NotNull <T extends CustomItemData> ItemStack createItemStack(@NotNull final CustomItem<T> customItem) {
        return this.createItemStack(customItem, customItem.createDefaultItemData());
    }

    boolean isCustomItem(@NotNull ItemStack itemStack);

    @NotNull ItemStack deserialize(@NotNull JsonObject jsonObject);
}
