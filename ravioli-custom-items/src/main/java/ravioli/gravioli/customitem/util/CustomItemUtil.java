package ravioli.gravioli.customitem.util;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.customitem.item.CustomItem;
import ravioli.gravioli.customitem.service.CustomItemService;

public final class CustomItemUtil {
    public static @NotNull JsonObject serialize(@NotNull final ItemStack itemStack) {
        final CustomItemService customItemService = Platform.loadService(CustomItemService.class);
        final CustomItem<?> customItem = customItemService.getCustomItem(itemStack)
                .orElseThrow();

        return customItem.serialize(itemStack);
    }

    public static @NotNull ItemStack deserialize(@NotNull final JsonObject jsonObject) {
        final CustomItemService customItemService = Platform.loadService(CustomItemService.class);

        return customItemService.deserialize(jsonObject);
    }
}
