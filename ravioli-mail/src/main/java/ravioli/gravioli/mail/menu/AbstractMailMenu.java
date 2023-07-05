package ravioli.gravioli.mail.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.mail.item.MailboxItem;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.mail.service.MailService;
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.provider.impl.AsyncPaginationMenuProvider;

import java.util.Objects;

public abstract class AbstractMailMenu extends Menu {
    protected final Plugin plugin;
    protected final MailService mailService;
    protected final AsyncPaginationMenuProvider pagination;

    public AbstractMailMenu(@NotNull final Plugin plugin,
                            @NotNull final Player player) {
        super(plugin, player);

        this.plugin = plugin;
        this.mailService = Objects.requireNonNull(Platform.loadService(MailService.class));

        final Mask mask = this.getMask();
        final MailboxType type = this.getMailboxType();
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));

        this.pagination = this.providers().register(new AsyncPaginationMenuProvider(mask, (page) -> {
            final CollectionFetchResult<@NotNull MailboxItem> results = this.mailService.fetchRange(
                            userService.getByUuid(player.getUniqueId()).orElseThrow(),
                            type,
                            page * mask.getSize(),
                            (page + 1) * mask.getSize()
                    )
                    .join();

            return new AsyncPaginationMenuProvider.PageData(
                    results.items()
                            .stream()
                            .map((mailboxItem) -> {
                                final ItemStack itemStack = this.convertToItemStack(mailboxItem);

                                return MenuComponent.item(itemStack, (event) -> this.onClick(event, mailboxItem));
                            })
                            .toList(),
                    results.hasNext()
            );
        }));
    }

    protected abstract MailboxType getMailboxType();

    /**
     * The mask that determines where content is positioned on the page as well as how much content
     * is displayed per page.
     *
     * @return a pagination mask
     * @implNote the result of this method is only utilized once, so returning a completely
     * new mask in this method is okay.
     */
    protected abstract Mask getMask();

    /**
     * Convert a given {@link MailboxItem} to an {@link ItemStack} that represents the contents of the
     * mail item. This is called when populating the menu with new or updated content.
     *
     * @param mailboxItem the MailboxItem to convert
     * @return a new ItemStack representative of the mail item
     */
    protected abstract @NotNull ItemStack convertToItemStack(@NotNull MailboxItem mailboxItem);

    /**
     * Called when an {@link ItemStack}, created from {@link #convertToItemStack(MailboxItem)}, is
     * clicked in the GUI.
     *
     * @param event       the bukkit {@link InventoryClickEvent} event that triggered this
     * @param mailboxItem the MailboxItem that is associated with the clicked item
     */
    protected abstract void onClick(@NotNull InventoryClickEvent event, @NotNull MailboxItem mailboxItem);
}
