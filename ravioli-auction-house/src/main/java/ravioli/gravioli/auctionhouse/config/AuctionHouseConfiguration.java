package ravioli.gravioli.auctionhouse.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;

import java.util.Objects;

public class AuctionHouseConfiguration {
    private String currencyId;

    public void load(@NotNull final FileConfiguration configFile) {
        this.currencyId = Objects.requireNonNull(configFile.getString("currency"));
    }

    public @NotNull Currency getCurrency() {
        return Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class))
            .getCurrency(this.currencyId)
            .orElseThrow();
    }
}
