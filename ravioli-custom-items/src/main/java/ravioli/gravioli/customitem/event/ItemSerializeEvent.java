package ravioli.gravioli.customitem.event;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.customitem.item.CustomItem;

import java.util.HashSet;
import java.util.Set;

public class ItemSerializeEvent extends Event {
    public static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final ItemStack itemStack;
    private final CustomItem<?> customItem;
    private final Set<JsonObject> additionalData = new HashSet<>();

    public ItemSerializeEvent(final boolean async, @NotNull final ItemStack itemStack,
                              @NotNull final CustomItem<?> customItem) {
        super(async);

        this.itemStack = itemStack;
        this.customItem = customItem;
    }

    public @NotNull CustomItem<?> customItem() {
        return this.customItem;
    }

    public @NotNull ItemStack itemStack() {
        return this.itemStack;
    }

    public @NotNull Set<JsonObject> additionalData() {
        return ImmutableSet.copyOf(this.additionalData);
    }

    public void addAdditionalData(@NotNull final JsonObject jsonObject) {
        this.additionalData.add(jsonObject);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
