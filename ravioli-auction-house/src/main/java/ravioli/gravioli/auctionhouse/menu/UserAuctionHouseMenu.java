package ravioli.gravioli.auctionhouse.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.menu.mail.ExpiredItemsMenu;
import ravioli.gravioli.auctionhouse.service.AuctionHouseService;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.locale.RavioliLocale;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.component.MenuComponent;

import java.util.Objects;

public class UserAuctionHouseMenu extends Menu {
    private final AuctionHouseService auctionHouseService;
    private final RavioliLocale<CommandSender> locale;

    public UserAuctionHouseMenu(@NotNull final Plugin plugin,
                                @NotNull final Player player,
                                @NotNull final RavioliLocale<CommandSender> locale) {
        super(plugin, player);

        this.auctionHouseService = Objects.requireNonNull(Platform.loadService(AuctionHouseService.class));
        this.locale = locale;
    }

    @Override
    public void init() {
        this.properties().height(3);
        this.setTitle(Component.text("Auction House"));
    }

    @Override
    public void update() {
        this.content().set(0, MenuComponent.item(
                new ItemBuilder(Material.ARROW)
                        .displayName("Go back")
                        .build(),
                (event) -> new AuctionHouseMenu(this.plugin(), this.player, this.locale).open()
        ));

        this.content().set(1, 1, MenuComponent.item(
                new ItemBuilder(Material.CHEST_MINECART)
                        .displayName("Your items")
                        .build(),
                (event) -> new UserListingsMenu(this.plugin(), this.player, this.locale).open()
        ));
        this.content().set(2, 1, MenuComponent.item(
                new ItemBuilder(Material.DROPPER)
                        .displayName("Your expired items")
                        .build(),
                (event) -> new ExpiredItemsMenu(
                        this.plugin(),
                        this.locale,
                        this.player
                ).open()
        ));
    }
}
