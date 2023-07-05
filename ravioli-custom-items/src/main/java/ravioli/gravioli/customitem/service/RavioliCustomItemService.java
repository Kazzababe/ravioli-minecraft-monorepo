package ravioli.gravioli.customitem.service;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.core.util.PluginUtil;
import ravioli.gravioli.customitem.config.item.VanillaItemDetails;
import ravioli.gravioli.customitem.data.CustomItemData;
import ravioli.gravioli.customitem.event.CustomItemCreationEvent;
import ravioli.gravioli.customitem.event.ItemDeserializeEvent;
import ravioli.gravioli.customitem.item.CustomItem;
import ravioli.gravioli.customitem.item.VanillaCustomItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RavioliCustomItemService implements CustomItemService {
    private static final NamespacedKey ITEM_KEY = new NamespacedKey("custom_items_api", "item_id");

    private final Map<String, CustomItem<?>> customItemById = new HashMap<>();
    private final Map<Class<? extends CustomItem>, CustomItem<?>> customItemByClass = new HashMap<>();

    private boolean callCreationEvent;

    public void setCallCreationEvent(final boolean callCreationEvent) {
        this.callCreationEvent = callCreationEvent;
    }

    @Override
    public @NotNull CustomItem<?> registerCustomItem(@NotNull final CustomItem<?> customItem) {
        this.customItemById.put(customItem.id(), customItem);
        this.customItemByClass.put(customItem.getClass(), customItem);

        return customItem;
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public @NotNull Optional<@NotNull CustomItem> getCustomItem(@NotNull final String id) {
        return Optional.ofNullable(this.customItemById.get(id));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull <T extends CustomItem> Optional<@NotNull T> getCustomItem(@NotNull final Class<T> customItemClass) {
        return Optional.ofNullable(this.customItemByClass.get(customItemClass))
            .map((customItem) -> (T) customItem);
    }

    @Override
    @SuppressWarnings({"rawtypes"})
    public @NotNull Optional<@NotNull CustomItem> getCustomItem(@NotNull final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return Optional.empty();
        }
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        final String customItemId = container.get(ITEM_KEY, PersistentDataType.STRING);

        if (customItemId == null) {
            return Optional.of(new VanillaCustomItem(itemStack.getType(), VanillaItemDetails.EMPTY));
        }
        return this.getCustomItem(customItemId);
    }

    @Override
    public @NotNull <T extends CustomItemData> ItemStack createItemStack(@NotNull final CustomItem<T> customItem,
                                                                         @NotNull final T data) {
        final ItemStack itemStack = this.createItemBuilder(customItem, data).build();

        if (!this.callCreationEvent) {
            return itemStack;
        }
        final CustomItemCreationEvent event = PluginUtil.callEvent(new CustomItemCreationEvent(itemStack, customItem, data));

        return event.itemStack();
    }

    @Override
    public boolean isCustomItem(@NotNull final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        return container.has(ITEM_KEY, PersistentDataType.STRING);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NotNull ItemStack deserialize(@NotNull final JsonObject jsonObject) {
        final String itemId = jsonObject.get("item").getAsString();
        final JsonObject dataObject = jsonObject.get("data").getAsJsonObject();
        final CustomItem customItem = this.getCustomItem(itemId).orElseThrow();
        final CustomItemData customItemData = customItem.deserializeData(dataObject);
        final int amount = jsonObject.has("amount") ? jsonObject.get("amount").getAsInt() : 1;
        final ItemBuilder itemBuilder = this.createItemBuilder(customItem, customItemData);
        final ItemDeserializeEvent event = PluginUtil.callEvent(
            new ItemDeserializeEvent(
                !Bukkit.isPrimaryThread(),
                dataObject,
                itemBuilder,
                customItem
            )
        );

        return event.itemBuilder().build();
    }

    private @NotNull <T extends CustomItemData> ItemBuilder createItemBuilder(@NotNull final CustomItem<T> customItem,
                                                                              @NotNull final T data) {
        return customItem.itemBuilder(data)
            .modifier(ItemMeta.class, (itemMeta) -> {
                final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                container.set(ITEM_KEY, PersistentDataType.STRING, customItem.id());
            });
    }
}
