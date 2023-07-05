package ravioli.gravioli.currency.overlay;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;
import ravioli.gravioli.common.currency.service.CurrencyService;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.component.Label;
import ravioli.gravioli.core.resourcepack.layout.panel.Panel;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;
import ravioli.gravioli.core.resourcepack.overlay.OverlayComponent;
import ravioli.gravioli.core.util.FormatUtil;

import java.util.Objects;

public class CurrencyOverlayComponent implements OverlayComponent {
    private final Currency primaryCurrency;

    public CurrencyOverlayComponent() {
        this.primaryCurrency = Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class))
            .getCurrency("global_dollars")
            .orElseThrow();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public @NotNull String id() {
        return "CURRENCY";
    }

    @Override
    public @NotNull ResourcePackComponent component(@NotNull final Player player) {
        final CurrencyService currencyService = Objects.requireNonNull(Platform.loadService(CurrencyService.class));
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));
        final User user = userService
            .getByUuid(player.getUniqueId())
            .orElseThrow();
        final String amount = FormatUtil.formatCommas(
            currencyService.load(user, this.primaryCurrency)
                .join()
                .intValue()
        );

        return new Panel.Builder(176)
            .child(
                new Label.Builder(amount + " ", "\uE001")
                    .contentModifiers(
                        (builder) -> builder.font(Key.key("ravioli", "default/-10")),
                        (builder) -> builder.font(Key.key("ravioli", "icons/-4"))
                    )
                    .alignment(Alignment.RIGHT)
                    .create()
            )
            .create();
    }
}
