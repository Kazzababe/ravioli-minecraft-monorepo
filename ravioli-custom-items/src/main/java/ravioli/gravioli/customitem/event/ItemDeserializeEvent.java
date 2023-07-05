package ravioli.gravioli.customitem.event;

import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.customitem.item.CustomItem;

public class ItemDeserializeEvent extends Event {
    public static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final JsonObject data;
    private final ItemBuilder itemBuilder;
    private final CustomItem<?> customItem;

    public ItemDeserializeEvent(final boolean async, @NotNull final JsonObject data,
                                @NotNull final ItemBuilder itemBuilder, @NotNull final CustomItem<?> customItem) {
        super(async);

        this.data = data;
        this.itemBuilder = itemBuilder;
        this.customItem = customItem;
    }

    public @NotNull CustomItem<?> customItem() {
        return this.customItem;
    }

    public @NotNull JsonObject data() {
        return this.data;
    }

    public @NotNull ItemBuilder itemBuilder() {
        return this.itemBuilder;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
