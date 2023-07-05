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
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.mask.StringMask;
import ravioli.gravioli.menu.property.MenuProperty;
import ravioli.gravioli.menu.provider.impl.AsyncPaginationMenuProvider;

import java.math.BigDecimal;
import java.util.Objects;

public class AuctionHouseMenu extends Menu {
    private static final Mask MASK = new StringMask("000000000 011111110 011111110 011111110 011111110");

    private final AuctionHouseService auctionHouseService;
    private final RavioliLocale<CommandSender> locale;

    private final AsyncPaginationMenuProvider paginationMenuProvider;
    private final MenuProperty<SortType> sortType;
    private final MenuProperty<SortDirection> sortDirection;

    public AuctionHouseMenu(@NotNull final Plugin plugin, @NotNull final Player player,
                            @NotNull final RavioliLocale<CommandSender> locale) {
        super(plugin, player);

        this.auctionHouseService = Objects.requireNonNull(Platform.loadService(AuctionHouseService.class));
        this.locale = locale;

        this.sortType = this.properties().create(SortType.CREATED_AT);
        this.sortDirection = this.properties().create(SortDirection.DESCENDING);
        this.paginationMenuProvider = this.providers().register(new AsyncPaginationMenuProvider(
            MASK,
            (page) -> {
                final CollectionFetchResult<AuctionHouseItem> results = this.auctionHouseService.fetchGlobalRange(
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
        this.setTitle(Component.text("Auction House"));
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

        this.content().set(1, 4, MenuComponent.item(
            new ItemBuilder(Material.CHEST)
                .displayName(Component.text("Your Auction House", NamedTextColor.GRAY))
                .lore(
                    MiniMessage.miniMessage().deserialize("<white><yellow>*</yellow> View your active listings."),
                    MiniMessage.miniMessage().deserialize("<white><yellow>*</yellow> View your expired listings."),
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<gray><green><u>Click</u></green> to continue.")
                )
                .build(),
            (event) -> new UserAuctionHouseMenu(this.plugin(), this.player, this.locale).open()
        ));
        this.content().set(4, 4, MenuComponent.item(
            new ItemBuilder(Material.BOOK)
                .displayName(MiniMessage.miniMessage().deserialize("<gray>What is the Auction House?"))
                .lore(
                    MiniMessage.miniMessage().deserialize("<white>The Auction House is a place to buy and"),
                    MiniMessage.miniMessage().deserialize("<white>sell items from and to other players."),
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<gray>How do I sell my items?"),
                    MiniMessage.miniMessage().deserialize("<white>Use the command /ah sell <price> while"),
                    MiniMessage.miniMessage().deserialize("<white>holding an item in your hand.")
                )
                .build()
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
        final User seller = auctionHouseItem.user();

        if (seller.uuid().equals(this.player.getUniqueId())) {
            this.locale.send(this.player, "purchase.purchase-own-item");

            return;
        }
        final User user = Objects.requireNonNull(Platform.loadService(UserService.class))
            .getByUuid(this.player.getUniqueId())
            .orElseThrow();
        final Object currency = auctionHouseItem.currency();
        final BigDecimal cost = auctionHouseItem.cost();

//        this.auctionHouseService.purchaseItem(user, auctionHouseItem)
//            .thenAccept((result) -> {
//                switch (result) {
//                    case NOT_ENOUGH_CURRENCY -> this.locale.send(
//                        this.player,
//                        "purchase.not-enough-currency",
//                        Placeholder.component("currency_name", /*currency.displayName(cost)*/Component.text("Dollars")),
//                        Placeholder.component("cost", Component.text(cost.toString())//currency.format(cost))
//                    );
//                    case LOCKED -> this.locale.send(this.player, "purchase.locked");
//                    case SUCCESS -> {
//                        final ItemStack itemStack = auctionHouseItem.itemStack();
//
//                        this.locale.send(
//                            this.player,
//                            "purchase.purchased",
//                            Placeholder.component("item", ItemUtil.getItemStackName(auctionHouseItem.itemStack())),
//                            Placeholder.component("currency_name", Component.text("Dollars")/*currency.displayName(cost)*/),
//                            Placeholder.component("cost", Component.text(cost.toString()) /*currency.format(cost)*/)
//                        );
//
//                        MailUtil.addToInventory(this.player, itemStack);
//                    }
//                    case GONE -> this.locale.send(this.player, "purchase.deleted");
//                }
//                this.paginationMenuProvider.resetPage();
//            })
//            .exceptionally((e) -> {
//                e.printStackTrace();
//                return null;
//            });
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
