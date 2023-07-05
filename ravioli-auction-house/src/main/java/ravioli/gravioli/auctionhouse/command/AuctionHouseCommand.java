package ravioli.gravioli.auctionhouse.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.config.AuctionHouseConfiguration;
import ravioli.gravioli.auctionhouse.data.AuctionHouseItem;
import ravioli.gravioli.auctionhouse.menu.AuctionHouseMenu;
import ravioli.gravioli.auctionhouse.service.AuctionHouseService;
import ravioli.gravioli.command.common.context.CommandContext;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.locale.RavioliLocale;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.core.command.RavioliCommand;
import ravioli.gravioli.mail.util.MailUtil;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class AuctionHouseCommand extends RavioliCommand {
    private final Plugin plugin;
    private final AuctionHouseService auctionHouseService;
    private final RavioliLocale<CommandSender> locale;
    private final AuctionHouseConfiguration auctionHouseConfiguration;

    public AuctionHouseCommand(@NotNull final Plugin plugin,
                               @NotNull final RavioliLocale<CommandSender> locale,
                               @NotNull final AuctionHouseService auctionHouseService,
                               @NotNull final AuctionHouseConfiguration auctionHouseConfiguration) {
        super("ah", "auctionhouse");

        this.plugin = plugin;
        this.auctionHouseConfiguration = auctionHouseConfiguration;
        this.auctionHouseService = auctionHouseService;
        this.locale = locale;
    }

    @Override
    protected void init() {
        this.executor(this::onMenu);

        this.registerSubCommand(
                DEFAULT_COMMAND_META,
                Literals("list", "list", "sell"),
                Integer("amount")
                        .withMin(1)
                        .asyncExecutor(this::onList)
        );
    }

    private void onMenu(@NotNull final CommandContext<CommandSender> context) {
        if (!(context.getSource() instanceof final Player player)) {
            return;
        }
        new AuctionHouseMenu(this.plugin, player, this.locale).open();
    }

    private void onList(@NotNull final CommandContext<CommandSender> context) {
        if (!(context.getSource() instanceof final Player player)) {
            return;
        }
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));
        final User user = userService.getByUuid(player.getUniqueId()).orElseThrow();
        final ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType().isAir()) {
            this.locale.send(player, "list.invalid-item");

            return;
        }
        final int amount = context.get("amount");
        final Currency currency = this.auctionHouseConfiguration.getCurrency();
        final BigDecimal cost = BigDecimal.valueOf(amount);
        final AuctionHouseItem auctionHouseItem = new AuctionHouseItem(-1, user.id(), itemStack, Instant.now(), Instant.now().plus(Duration.ofSeconds(5)), currency, cost);

        player.getInventory().setItemInMainHand(null);

        this.auctionHouseService.create(auctionHouseItem)
                .thenRun(() -> this.locale.send(player, "list.success"))
                .exceptionally((e) -> {
                    MailUtil.addToInventory(player, itemStack);

                    return null;
                });
    }
}
