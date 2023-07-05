package ravioli.gravioli.customitem.item;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.customitem.action.CustomItemAction;
import ravioli.gravioli.customitem.action.event.CustomItemEvent;
import ravioli.gravioli.customitem.behavior.CustomItemFlag;
import ravioli.gravioli.customitem.data.CustomItemData;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public interface CustomItem<T extends CustomItemData> {
    @NotNull String id();

    @NotNull ItemBuilder itemBuilder(@NotNull T itemData);

    default @NotNull ItemBuilder newItemBuilder() {
        return this.itemBuilder(this.createDefaultItemData());
    }

    @NotNull JsonObject serialize(@NotNull ItemStack itemStack);

    @NotNull T extractItemData(@NotNull ItemStack itemStack);

    @NotNull T createDefaultItemData();

    @NotNull T deserializeData(@NotNull JsonObject jsonObject);

    @NotNull Collection<@NotNull CustomItemFlag> itemFlags();

    boolean hasFlag(@NotNull CustomItemFlag flag);

    boolean hasAction(@NotNull CustomItemAction<?> customItemAction);

    @NotNull <K extends CustomItemEvent> Optional<@NotNull Consumer<@NotNull K>> actionHandler(@NotNull CustomItemAction<K> customItemAction);
}
