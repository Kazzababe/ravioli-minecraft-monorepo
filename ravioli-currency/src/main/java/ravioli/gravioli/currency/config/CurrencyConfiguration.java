package ravioli.gravioli.currency.config;

import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CurrencyConfiguration implements CurrencyConfigurationService {
    private final Map<String, Currency> registeredCurrencies = new HashMap<>();

    public void reload(@NotNull final FileConfiguration configFile) {
        final ConfigurationSection rootSection = configFile.getConfigurationSection("currencies");

        this.registeredCurrencies.clear();

        if (rootSection == null) {
            return;
        }
        for (final String key : rootSection.getKeys(false)) {
            final ConfigurationSection currencySection = rootSection.getConfigurationSection(key);

            if (currencySection == null) {
                continue;
            }
            final String id = currencySection.getString("id");
            final ConfigurationSection displaySection = currencySection.getConfigurationSection("display");
            final ConfigurationSection formatSection = currencySection.getConfigurationSection("format");

            try {
                Preconditions.checkArgument(id != null, "Missing \"id\" field for currency.");
                Preconditions.checkArgument(displaySection != null, "Missing \"display\" section for currency \"" + id + "\".");
                Preconditions.checkArgument(formatSection != null, "Missing \"format\" section for currency \"" + id + "\".");

                final String format = formatSection.getString("format", "<value>");
                final int decimalPlaces = formatSection.getInt("decimals", 0);
                final String singularDisplay = displaySection.getString("singular");
                final String pluralDisplay = displaySection.getString("plural");

                Preconditions.checkArgument(singularDisplay != null, "Missing \"display.singular\" field for currency \"" + id + "\".");
                Preconditions.checkArgument(pluralDisplay != null, "Missing \"display.plural\" field for currency \"" + id + "\".");

                this.registeredCurrencies.put(
                        id,
                        new Currency(
                                id,
                                singularDisplay,
                                pluralDisplay,
                                format,
                                decimalPlaces
                        )
                );
            } catch (final Exception e) {
                continue;
            }
        }
    }

    @Override
    public @NotNull Optional<@NotNull Currency> getCurrency(@NotNull final String id) {
        return Optional.ofNullable(this.registeredCurrencies.get(id));
    }
}
