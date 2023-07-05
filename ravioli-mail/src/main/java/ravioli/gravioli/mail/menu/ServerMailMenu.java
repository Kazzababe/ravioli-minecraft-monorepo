package ravioli.gravioli.mail.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.util.ComponentWrapper;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.mail.item.MailboxItem;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.mask.StringMask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ServerMailMenu extends AbstractMailMenu {

    private volatile boolean processing;

    public ServerMailMenu(@NotNull final Plugin plugin,
                          @NotNull final Player player) {
        super(plugin, player);
    }

    @Override
    public void init() {
        this.properties().height(6);
    }

    @Override
    public void update() {
        this.content().add(this.pagination);
        this.content().set(0, 4, MenuComponent.item(
            new ItemBuilder(Material.ARROW)
                .displayName(Component.text("Previous page"))
                .build(),
            (event) -> {
                if (this.pagination.hasPrevious()) {
                    this.pagination.previous();
                }
            }));
        this.content().set(1, 4, MenuComponent.item(
            new ItemBuilder(false ? Material.REDSTONE_TORCH : Material.LEVER)
                .displayName(Component.text("Delete mail after opening."))
                .build(),
            (event) -> {
//                                    final User user = this.userService
//                                            .get(this.player)
//                                            .orElseThrow();
//                                    final boolean newValue = !this.deleteAfterOpen.get();
//
//                                    this.userSettingRepository.set(user, Setting.DELETE_MAIL_ON_OPEN, newValue);
//                                    this.pagination.refresh();
//                                    this.deleteAfterOpen.set(newValue);
            }));
        this.content().set(8, 4, MenuComponent.item(
            new ItemBuilder(Material.ARROW)
                .displayName(Component.text("Next page"))
                .build(),
            (event) -> {
                if (this.pagination.hasNext()) {
                    this.pagination.next();
                }
            }));
    }

    private void claimItem(@NotNull final MailboxItem mailboxItem) {
        final MailboxItem.ClaimResult claimResult = mailboxItem.claim(this.player);

        if (claimResult == MailboxItem.ClaimResult.IGNORE) {
            return;
        }
        if (claimResult == MailboxItem.ClaimResult.CLAIMED) {
            this.player.sendMessage("Claimed this item!");
            this.pagination.refresh();
        }
    }

    @Override
    protected MailboxType getMailboxType() {
        return MailboxType.SERVER;
    }

    @Override
    protected Mask getMask() {
        return new StringMask("000000000 011111110 011111110 011111110 011111110");
    }

    @Override
    protected @NotNull ItemStack convertToItemStack(@NotNull MailboxItem mailboxItem) {
        final ItemStack itemStack = mailboxItem.icon();
        final ItemMeta itemMeta = itemStack.getItemMeta();
        final Component displayName = itemMeta.displayName();
        final List<Component> lore = Objects.requireNonNullElse(itemMeta.lore(), new ArrayList<>());

        if (mailboxItem.opened()) {
            itemMeta.setCustomModelData(mailboxItem.category().readModelData());
        } else {
            itemMeta.setCustomModelData(mailboxItem.category().unreadModelData());
        }
        if (displayName != null) {
            itemMeta.displayName(
                displayName
                    .replaceText(
                        TextReplacementConfig.builder()
                            .match("<title>")
                            .replacement(Objects.requireNonNullElse(
                                mailboxItem.title(),
                                "Mail"
                            ))
                            .build()
                    )
                    .replaceText(
                        TextReplacementConfig.builder()
                            .match("<received_exact>")
                            .replacement(Component.text(
                                mailboxItem.createdOn().toString()
                            ))
                            .build()
                    ).replaceText(
                        TextReplacementConfig.builder()
                            .match("<received_relative>")
                            .replacement(Component.text(
//                                                    TIME_AGO.timeAgo(mailboxItem.createdOn().toEpochMilli())
                            ))
                            .build()
                    )
                    .replaceText(
                        TextReplacementConfig.builder()
                            .match("<message>")
                            .replacement(Objects.requireNonNullElse(
                                mailboxItem.message(),
                                ""
                            ))
                            .build()
                    )
            );
        }
        final List<Component> newLore = new ArrayList<>();

        for (final Component line : lore) {
            final List<Component> newLines = ComponentWrapper.wrap(
                line
                    .replaceText(
                        TextReplacementConfig.builder()
                            .match("<title>")
                            .replacement(Objects.requireNonNullElse(
                                mailboxItem.title(),
                                "Mail"
                            ))
                            .build()
                    )
                    .replaceText(
                        TextReplacementConfig.builder()
                            .match("<received_exact>")
                            .replacement(Component.text(
                                mailboxItem.createdOn().toString()
                            ))
                            .build()
                    ).replaceText(
                        TextReplacementConfig.builder()
                            .match("<received_relative>")
                            .replacement(Component.text(
//                                                    TIME_AGO.timeAgo(mailboxItem.createdOn().toEpochMilli())
                            ))
                            .build()
                    )
                    .replaceText(
                        TextReplacementConfig.builder()
                            .match("<message>")
                            .replacement(Objects.requireNonNullElse(
                                mailboxItem.message(),
                                ""
                            ))
                            .build()
                    ),
                40
            );

            newLore.addAll(newLines);
        }
        itemMeta.lore(newLore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    protected void onClick(@NotNull final InventoryClickEvent event, @NotNull final MailboxItem mailboxItem) {
        if (this.processing) {
            return;
        }
        final boolean delete = false;//this.deleteAfterOpen.get();

        this.processing = true;

//        if (!mailboxItem.serverTarget().valid()) {
//            this.player.sendMessage("You cannot open this on this server!");
//
//            return;
//        }
        if (mailboxItem.opened()) {
            this.claimItem(mailboxItem);

            if (delete) {
                this.mailService.delete(mailboxItem).whenComplete((input, e) -> {
                    this.pagination.refresh();
                    this.processing = false;
                });
            } else {
                this.processing = false;
            }
            return;
        }
        final CompletableFuture<Boolean> future;

        if (delete) {
            future = this.mailService
                .delete(mailboxItem);
        } else {
            future = this.mailService.setOpened(mailboxItem, true);
        }
        future.whenCompleteAsync((input, e) -> {
            if (input) {
                this.claimItem(mailboxItem);
            }
            this.processing = false;
        }, Bukkit.getScheduler().getMainThreadExecutor(this.plugin));
    }
}
