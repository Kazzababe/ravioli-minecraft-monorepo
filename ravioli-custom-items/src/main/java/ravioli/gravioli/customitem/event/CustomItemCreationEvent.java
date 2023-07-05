package ravioli.gravioli.customitem.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.customitem.data.CustomItemData;
import ravioli.gravioli.customitem.item.CustomItem;

public class CustomItemCreationEvent extends Event {
    public static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final ItemStack itemStack;
    private final CustomItem<?> customItem;
    private final CustomItemData customItemData;

    public CustomItemCreationEvent(@NotNull final ItemStack itemStack, @NotNull final CustomItem<?> customItem,
                                   @NotNull final CustomItemData customItemData) {
        this.itemStack = itemStack;
        this.customItem = customItem;
        this.customItemData = customItemData;
    }

    public @NotNull ItemStack itemStack() {
        return this.itemStack;
    }

    public @NotNull CustomItem<?> customItem() {
        return this.customItem;
    }

    public @NotNull CustomItemData customItemData() {
        return this.customItemData;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
