package ravioli.gravioli.currency.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.command.common.context.CommandContext;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;
import ravioli.gravioli.common.currency.service.CurrencyService;
import ravioli.gravioli.common.currency.transaction.Transaction;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.core.command.RavioliCommand;

import java.math.BigDecimal;
import java.util.Objects;

public class CurrencyCommand extends RavioliCommand {
    public CurrencyCommand() {
        super("currency");
    }

    @Override
    protected void init() {
        this.registerSubCommand(
            DEFAULT_COMMAND_META,
            Literals("give", "give", "deposit")
                .exceptionally((source, e) -> {e.printStackTrace();}),
            String("player")
                .exceptionally((source, e) -> {e.printStackTrace();}),
            String("currency")
                .exceptionally((source, e) -> {e.printStackTrace();}),
            Integer("amount")
                .withMin(0)
                .clamp(true)
                .exceptionally((source, e) -> {e.printStackTrace();})
                .asyncExecutor(this::onGiveCurrency)
        );
        this.registerSubCommand(
            DEFAULT_COMMAND_META,
            Literals("take", "take", "withdraw", "remove")
                .exceptionally((source, e) -> {e.printStackTrace();}),
            String("player")
                .exceptionally((source, e) -> {e.printStackTrace();}),
            String("currency")
                .exceptionally((source, e) -> {e.printStackTrace();}),
            Integer("amount")
                .withMin(0)
                .clamp(true)
                .exceptionally((source, e) -> {e.printStackTrace();})
                .asyncExecutor(this::onTakeCurrency)
        );
    }

    private void onGiveCurrency(@NotNull final CommandContext<CommandSender> context) {
        final String playerName = context.get("player");
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));
        final User user = userService
            .getOrLoadByUsername(playerName)
            .join();

        if (user == null) {
            context.getSource().sendMessage("NO TARGET");

            return;
        }
        final CurrencyService currencyService = Objects.requireNonNull(Platform.loadService(CurrencyService.class));
        final CurrencyConfigurationService currencyConfigurationService = Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class));
        final String currencyId = context.get("currency");
        final Currency currency = currencyConfigurationService.getCurrency(currencyId)
                .orElse(null);

        if (currency == null) {
            context.getSource().sendMessage("BAD CURRENCY ID");

            return;
        }
        final int amount = context.get("amount");

        currencyService.deposit(user, new Transaction(user, currency, BigDecimal.valueOf(amount)))
            .thenAccept((result) -> {
                context.getSource().sendMessage("SUCCESS: " + result.success() + ", " + result.balance());
            });
    }

    private void onTakeCurrency(@NotNull final CommandContext<CommandSender> context) {
        final String playerName = context.get("player");
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));
        final User user = userService
            .getOrLoadByUsername(playerName)
            .join();

        if (user == null) {
            context.getSource().sendMessage("NO TARGET");

            return;
        }
        final CurrencyService currencyService = Objects.requireNonNull(Platform.loadService(CurrencyService.class));
        final CurrencyConfigurationService currencyConfigurationService = Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class));
        final String currencyId = context.get("currency");
        final Currency currency = currencyConfigurationService.getCurrency(currencyId)
            .orElse(null);

        if (currency == null) {
            context.getSource().sendMessage("BAD CURRENCY ID");

            return;
        }
        final int amount = context.get("amount");

        currencyService.withdraw(user, new Transaction(user, currency, BigDecimal.valueOf(amount)))
            .thenAccept((result) -> {
                context.getSource().sendMessage("SUCCESS: " + result.success() + ", " + result.balance());
            });
    }
}
