package ravioli.gravioli.common.currency.service;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;

import java.util.Optional;

public interface CurrencyConfigurationService {
    @NotNull Optional<@NotNull Currency> getCurrency(@NotNull String id);
}
