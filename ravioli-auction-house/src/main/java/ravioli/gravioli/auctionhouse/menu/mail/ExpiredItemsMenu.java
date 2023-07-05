package ravioli.gravioli.auctionhouse.menu.mail;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.menu.UserAuctionHouseMenu;
import ravioli.gravioli.auctionhouse.service.AuctionHouseService;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.locale.RavioliLocale;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.mail.item.MailboxItem;
import ravioli.gravioli.mail.menu.AbstractMailMenu;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.mask.StringMask;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExpiredItemsMenu extends AbstractMailMenu {
    private final AuctionHouseService auctionHouseService;
    private final RavioliLocale<CommandSender> locale;
    private final AtomicBoolean processing = new AtomicBoolean(false);

    public ExpiredItemsMenu(@NotNull final Plugin plugin,
                            @NotNull final RavioliLocale<CommandSender> locale,
                            @NotNull final Player player) {
        super(plugin, player);

        this.auctionHouseService = Objects.requireNonNull(Platform.loadService(AuctionHouseService.class));
        this.locale = locale;
    }

    @Override
    protected MailboxType getMailboxType() {
        return MailboxType.AUCTION_HOUSE;
    }

    @Override
    protected Mask getMask() {
        return new StringMask("000000000 011111110 011111110 011111110");
    }

    @Override
    protected @NotNull ItemStack convertToItemStack(@NotNull final MailboxItem mailboxItem) {
        final ItemBuilder itemBuilder = new ItemBuilder(mailboxItem.icon());

        return itemBuilder.build();
    }

    @Override
    protected void onClick(@NotNull final InventoryClickEvent inventoryClickEvent,
                           @NotNull final MailboxItem mailboxItem) {
        if (this.processing.get()) {
            return;
        }
        final MailboxItem.ClaimResult claimResult = mailboxItem.claim(this.player);

        if (claimResult == MailboxItem.ClaimResult.IGNORE) {
            return;
        }
        if (claimResult == MailboxItem.ClaimResult.FAILED) {
            return;
        }
        this.processing.set(true);

        this.mailService.delete(mailboxItem)
                .whenComplete((result, e) -> {
                    this.processing.set(false);
                    this.pagination.refresh();
                });
    }

    @Override
    public void init() {
        this.properties().height(5);
        this.setTitle(Component.text("Expired listings (Page 1)"));
    }

    @Override
    public void update() {
        this.setTitle(Component.text("Expired listings (Page " + (this.pagination.page() + 1) + ")"));
        this.content().add(this.pagination);
        this.content().set(4, 4, MenuComponent.item(
                new ItemBuilder(Material.ARROW)
                        .displayName("Go back")
                        .build(),
                (event) -> new UserAuctionHouseMenu(this.plugin(), this.player, this.locale).open()
        ));
        this.content().set(0, 4, MenuComponent.item(
                new ItemBuilder(Material.ARROW)
                        .displayName(Component.text("Previous page"))
                        .build(),
                (event) -> {
                    if (this.pagination.hasPrevious()) {
                        this.pagination.previous();
                    }
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
}
