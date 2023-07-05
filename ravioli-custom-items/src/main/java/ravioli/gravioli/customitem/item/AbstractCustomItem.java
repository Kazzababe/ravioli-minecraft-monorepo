package ravioli.gravioli.customitem.item;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.util.PluginUtil;
import ravioli.gravioli.customitem.action.CustomItemAction;
import ravioli.gravioli.customitem.action.event.CustomItemEvent;
import ravioli.gravioli.customitem.behavior.CustomItemFlag;
import ravioli.gravioli.customitem.data.CustomItemData;
import ravioli.gravioli.customitem.event.ItemSerializeEvent;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractCustomItem<T extends CustomItemData> implements CustomItem<T> {
    private final Set<CustomItemFlag> flags = new HashSet<>();
    private final Map<CustomItemAction<?>, Consumer<? extends CustomItemEvent>> actions = new HashMap<>();
    private final String id;

    public AbstractCustomItem(@NotNull final String id) {
        this.id = id;
    }

    protected final <K extends CustomItemEvent> void addActionHandler(@NotNull final CustomItemAction<K> customItemAction,
                                                                      @NotNull Consumer<K> handler) {
        this.actions.put(customItemAction, handler);
    }

    protected final void addItemFlag(@NotNull final CustomItemFlag itemFlag) {
        this.flags.add(itemFlag);
    }

    @Override
    public @NotNull String id() {
        return this.id;
    }

    @Override
    public @NotNull JsonObject serialize(@NotNull final ItemStack itemStack) {
        final JsonObject jsonObject = new JsonObject();
        final T data = this.extractItemData(itemStack);
        final JsonObject dataJson = data.data();

        jsonObject.addProperty("item", this.id);
        jsonObject.addProperty("amount", itemStack.getAmount());

        final ItemSerializeEvent event = PluginUtil.callEvent(new ItemSerializeEvent(!Bukkit.isPrimaryThread(), itemStack, this));

        event.additionalData().forEach((additionalData) -> {
            additionalData.keySet().forEach((key) -> dataJson.add(key, additionalData.get(key)));
        });
        jsonObject.add("data", dataJson);

        return jsonObject;
    }

    @Override
    public @NotNull Collection<@NotNull CustomItemFlag> itemFlags() {
        return ImmutableSet.copyOf(this.flags);
    }

    @Override
    public boolean hasFlag(@NotNull final CustomItemFlag flag) {
        return this.flags.contains(flag);
    }

    @Override
    public boolean hasAction(@NotNull final CustomItemAction<?> customItemAction) {
        return this.actions.containsKey(customItemAction);
    }

    @Override
    public @NotNull <K extends CustomItemEvent> Optional<@NotNull Consumer<@NotNull K>> actionHandler(@NotNull final CustomItemAction<K> customItemAction) {
        return Optional.ofNullable(this.actions.get(customItemAction))
                .map((handler) -> (Consumer<K>) handler);
    }
}
