package ravioli.gravioli.mail.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.core.util.SchedulerUtil;
import ravioli.gravioli.customitem.service.CustomItemService;
import ravioli.gravioli.mail.item.MailboxItemItemStack;
import ravioli.gravioli.mail.model.MailboxItemCategory;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.mail.service.MailService;

import java.util.*;

public class MailUtil {
    /**
     * Add a {@link ItemStack} to a {@link Player Player's} inventory placing any items that do
     * not fit into the player's server mailbox.
     *
     * @param player    the player to give the item to
     * @param itemStack the item to give to the player
     * @throws NullPointerException if either the {@link MailService} or {@link UserService} is
     *                              unavailable
     */
    public static void addToInventory(@NotNull final Player player, @NotNull final ItemStack itemStack) {
        SchedulerUtil.sync().execute(() -> {
            final Collection<ItemStack> forMailbox;

            if (player.isOnline()) {
                forMailbox = player.getInventory().addItem(itemStack).values();
            } else {
                forMailbox = Set.of(itemStack);
            }
            if (forMailbox.isEmpty()) {
                return;
            }
            final MailService mailService = Objects.requireNonNull(Platform.loadService(MailService.class));
            final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));

            userService.loadByUuid(player.getUniqueId()).thenCompose((user) -> {
                if (user == null) {
                    throw new RuntimeException("User for player object not found.");
                }
                return mailService.create(
                    new MailboxItemItemStack(
                        user.id(),
                        MailboxType.SERVER,
                        MailboxItemCategory.GENERAL,
                        "Items for you!",
                        "Oops! This didn't fit in your inventory\nearlier so we had it sent to your mailbox!",
                        forMailbox.toArray(ItemStack[]::new)
                    )
                );
            });
        });
    }

    public static @NotNull ItemStack[] itemStacksFromJson(@NotNull final JsonArray itemStackJsonArray) {
        final List<ItemStack> itemStacks = new ArrayList<>();

        for (final JsonElement jsonElement : itemStackJsonArray) {
            itemStacks.add(itemStackFromJson(jsonElement.getAsJsonObject()));
        }
        return itemStacks.toArray(ItemStack[]::new);
    }

    public static @NotNull ItemStack itemStackFromJson(@NotNull final JsonObject jsonObject) {
        final CustomItemService customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));

        return customItemService.deserialize(jsonObject);
    }
}
