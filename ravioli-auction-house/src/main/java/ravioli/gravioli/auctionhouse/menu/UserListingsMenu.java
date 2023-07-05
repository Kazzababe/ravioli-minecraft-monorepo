package ravioli.gravioli.auctionhouse.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.data.AuctionHouseItem;
import ravioli.gravioli.auctionhouse.filter.AuctionFilter;
import ravioli.gravioli.auctionhouse.model.SortDirection;
import ravioli.gravioli.auctionhouse.model.SortType;
import ravioli.gravioli.auctionhouse.service.AuctionHouseService;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.locale.RavioliLocale;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.mail.util.MailUtil;
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.mask.StringMask;
import ravioli.gravioli.menu.property.MenuProperty;
import ravioli.gravioli.menu.provider.impl.AsyncPaginationMenuProvider;

import java.util.Objects;

public class UserListingsMenu extends Menu {
    private static final Mask MASK = new StringMask("000000000 011111110 011111110 011111110 011111110");

    private final AuctionHouseService auctionHouseService;
    private final RavioliLocale<CommandSender> locale;
    private final User user;

    private final AsyncPaginationMenuProvider paginationMenuProvider;
    private final MenuProperty<SortType> sortType;
    private final MenuProperty<SortDirection> sortDirection;

    public UserListingsMenu(@NotNull final Plugin plugin,
                            @NotNull final Player player,
                            @NotNull final RavioliLocale<CommandSender> locale) {
        super(plugin, player);

        this.auctionHouseService = Objects.requireNonNull(Platform.loadService(AuctionHouseService.class));
        this.locale = locale;
        this.user = Objects.requireNonNull(Platform.loadService(UserService.class))
            .getByUuid(player.getUniqueId())
            .orElseThrow();


        this.sortType = this.properties().create(SortType.CREATED_AT);
        this.sortDirection = this.properties().create(SortDirection.DESCENDING);
        this.paginationMenuProvider = this.providers().register(new AsyncPaginationMenuProvider(
            MASK,
            (page) -> {
                final CollectionFetchResult<AuctionHouseItem> results = this.auctionHouseService.fetchUserRange(
                        this.user,
                        page * MASK.getSize(),
                        (page + 1) * MASK.getSize(),
                        new AuctionFilter(
                            this.sortDirection.get(),
                            this.sortType.get()
                        )
                    )
                    .join();

                return new AsyncPaginationMenuProvider.PageData(
                    results.items()
                        .stream()
                        .map((auctionHouseItem) -> {
                            final ItemStack itemStack = this.convertToItemStack(auctionHouseItem);

                            return MenuComponent.item(itemStack, (event) -> this.onClick(event, auctionHouseItem));
                        })
                        .toList(),
                    results.hasNext()
                );
            })
        );
    }

    @Override
    public void init() {
        this.properties().height(5);
        this.setTitle(Component.text("Your listings"));
    }

    @Override
    public void update() {
        this.content().add(this.paginationMenuProvider);

        this.content().set(0, 4, MenuComponent.item(
            new ItemBuilder(Material.ARROW)
                .displayName(Component.text("Previous page"))
                .build(),
            (event) -> {
                if (this.paginationMenuProvider.hasPrevious()) {
                    this.paginationMenuProvider.previous();
                }
            }));
        this.content().set(8, 4, MenuComponent.item(
            new ItemBuilder(Material.ARROW)
                .displayName(Component.text("Next page"))
                .build(),
            (event) -> {
                if (this.paginationMenuProvider.hasNext()) {
                    this.paginationMenuProvider.next();
                }
            }));

        this.content().set(4, 4, MenuComponent.item(
            new ItemBuilder(Material.ARROW)
                .displayName("Go back")
                .build(),
            (event) -> new UserAuctionHouseMenu(this.plugin(), this.player, this.locale).open()
        ));
        this.content().set(7, 4, MenuComponent.item(
            new ItemBuilder(Material.HOPPER)
                .displayName(Component.text("Sort Items", NamedTextColor.GRAY))
                .lore(
                    Component.text("Sort Direction: " + this.sortDirection.get().displayName(), NamedTextColor.GRAY),
                    Component.text("Sort Type: " + this.sortType.get().displayName(), NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("Shift-click to toggle sort direction.", NamedTextColor.GRAY),
                    Component.text("Click to toggle sort type.", NamedTextColor.GRAY)
                )
                .build(),
            (event) -> {
                if (event.isShiftClick()) {
                    int newIndex = this.sortDirection.get().ordinal() + 1;

                    if (newIndex >= SortDirection.values().length) {
                        newIndex = 0;
                    }
                    this.sortDirection.set(SortDirection.values()[newIndex]);
                } else {
                    int newIndex = this.sortType.get().ordinal() + 1;

                    if (newIndex >= SortType.values().length) {
                        newIndex = 0;
                    }
                    this.sortType.set(SortType.values()[newIndex]);
                }
                this.paginationMenuProvider.resetPage();
            }
        ));
    }

    private void onClick(@NotNull final InventoryClickEvent event, @NotNull final AuctionHouseItem auctionHouseItem) {
        this.auctionHouseService.delete(auctionHouseItem).thenAccept((result) -> {
            if (!result) {
                this.locale.send(this.player, "remove-listing.purchased");

                return;
            }
            this.locale.send(this.player, "remove-listing.success");
            MailUtil.addToInventory(this.player, auctionHouseItem.itemStack());

            this.paginationMenuProvider.resetPage();
        });
    }

    private @NotNull ItemStack convertToItemStack(@NotNull final AuctionHouseItem auctionHouseItem) {
        final ItemBuilder itemBuilder = new ItemBuilder(auctionHouseItem.itemStack())
            .addLore(
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<gray>-----------------"),
                MiniMessage.miniMessage().deserialize("<gray>Cost: ").append(auctionHouseItem.currency().format(auctionHouseItem.cost())),
                MiniMessage.miniMessage().deserialize("<gray>Poster: <white>" + auctionHouseItem.user().username()),
                MiniMessage.miniMessage().deserialize("<gray>Ends on: <white> " + auctionHouseItem.expiration())
            );

        return itemBuilder.build();
    }
}
