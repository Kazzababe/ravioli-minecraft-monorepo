package ravioli.gravioli.mail.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.util.InventoryUtil;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.customitem.item.CustomItem;
import ravioli.gravioli.customitem.service.CustomItemService;
import ravioli.gravioli.mail.model.MailboxItemCategory;
import ravioli.gravioli.mail.model.MailboxItemType;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.mail.util.MailUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MailboxItemItemStack extends AbstractMailboxItem {
    private final ItemStack[] itemStacks;

    public MailboxItemItemStack(final long userId,
                                @NotNull final MailboxType mailboxType,
                                @NotNull final MailboxItemCategory mailboxItemCategory,
                                @Nullable final String title,
                                @Nullable final String message,
                                @NotNull final ItemStack[] itemStacks
    ) {
        this(-1, userId, mailboxType, mailboxItemCategory, title, message, Instant.now(), Instant.now(), false, itemStacks);
    }

    public MailboxItemItemStack(
            final long id,
            final long userId,
            @NotNull final MailboxType mailboxType,
            @NotNull final MailboxItemCategory category,
            @Nullable final String title,
            @Nullable final String message,
            @NotNull final Instant createdOn,
            @NotNull final Instant updatedOn,
            final boolean opened,
            @NotNull final ItemStack[] itemStacks) {
        super(id, userId, mailboxType, category, title, message, createdOn, updatedOn, opened);

        this.itemStacks = itemStacks;
    }

    public @NotNull ItemStack[] itemStacks() {
        return this.itemStacks;
    }

    @Override
    public @NotNull MailboxItemType type() {
        return MailboxItemType.ITEM;
    }

    @Override
    public @NotNull MailboxItemItemStack withId(final long id) {
        return new MailboxItemItemStack(
                id,
                this.userId,
                this.mailboxType,
                this.mailboxItemCategory,
                this.title,
                this.message,
                this.createdOn,
                this.updatedOn,
                this.opened,
                this.itemStacks
        );
    }

    @Override
    public @NotNull JsonObject toJson() {
        final JsonObject jsonObject = this.generateBaseJson();
        final JsonArray itemsArray = this.generateItemStackArray();

        jsonObject.add("items", itemsArray);

        return jsonObject;
    }

    @Override
    public @NotNull JsonElement data() {
        return this.generateItemStackArray();
    }

    private @NotNull JsonArray generateItemStackArray() {
        final JsonArray itemsArray = new JsonArray();

        for (final ItemStack itemStack : this.itemStacks) {
            final JsonObject itemJsonObject = this.generateItemStackJson(itemStack);

            itemsArray.add(itemJsonObject);
        }
        return itemsArray;
    }

    private @NotNull JsonObject generateItemStackJson(@NotNull final ItemStack itemStack) {
        final CustomItemService customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));
        final CustomItem<?> customItem = customItemService.getCustomItem(itemStack)
                .orElseThrow();

        return customItem.serialize(itemStack);
    }

    @Override
    public @NotNull ItemStack icon() {
        final ItemBuilder itemBuilder = new ItemBuilder(this.itemStacks[0])
                .displayName(Objects.requireNonNullElse(this.message, "Received mail!"))
                .addLore(
                        Component.text("Received on: " + this.createdOn, NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("Click to claim!", NamedTextColor.GREEN));

        return itemBuilder.build();
    }

    @Override
    public @NotNull MailboxItem.ClaimResult claim(@NotNull final Player player) {
        if (this.opened) {
            return ClaimResult.IGNORE;
        }
        for (final ItemStack itemStack : this.itemStacks) {
            if (!InventoryUtil.fits(player.getInventory(), itemStack)) {
                return ClaimResult.FAILED;
            }
        }
        for (final ItemStack itemStack : this.itemStacks) {
            player.getInventory().addItem(itemStack);
        }
        return ClaimResult.CLAIMED;
    }

    @Override
    public @NotNull JsonObject serialize() {
        return this.toJson();
    }

    public static @NotNull MailboxItemItemStack deserialize(@NotNull final JsonObject serializedData) {
        final JsonArray itemStackJsonArray = serializedData.getAsJsonArray("items");
        final ItemStack[] itemStacks = MailUtil.itemStacksFromJson(itemStackJsonArray);

        return new MailboxItemItemStack(
                serializedData.get("id").getAsLong(),
                serializedData.get("user-id").getAsLong(),
                MailboxType.valueOf(serializedData.get("mailbox-type").getAsString()),
                MailboxItemCategory.valueOf(serializedData.get("mailbox-category").getAsString()),
                serializedData.has("title") ? serializedData.get("title").getAsString() : null,
                serializedData.has("message") ? serializedData.get("message").getAsString() : null,
                Instant.ofEpochMilli(serializedData.get("created-on").getAsLong()),
                Instant.ofEpochMilli(serializedData.get("updated-on").getAsLong()),
                serializedData.get("opened").getAsBoolean(),
                itemStacks
        );
    }
}
