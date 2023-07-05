package ravioli.gravioli.common.currency.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Currency {
    private final String id;
    private final String rawDisplayName;
    private final Component displayName;
    private final String rawPluralDisplayName;
    private final Component pluralDisplayName;
    private final String format;
    private final int decimalPlaces;

    public Currency(@NotNull final String id, @NotNull final String rawDisplayName,
                    @NotNull final String rawPluralDisplayName, @NotNull final String format,
                    final int decimalPlaces) {
        this.id = id;
        this.rawDisplayName = rawDisplayName;
        this.displayName = MiniMessage.miniMessage().deserialize(rawDisplayName);
        this.rawPluralDisplayName = rawPluralDisplayName;
        this.pluralDisplayName = MiniMessage.miniMessage().deserialize(rawPluralDisplayName);
        this.format = format;
        this.decimalPlaces = decimalPlaces;
    }

    public @NotNull String id() {
        return this.id;
    }

    public @NotNull Component format(@NotNull final BigDecimal value) {
        return MiniMessage.miniMessage().deserialize(this.rawFormat(value));
    }

    public @NotNull Component displayName(@NotNull final BigDecimal value) {
        if (value.equals(BigDecimal.ONE)) {
            return this.displayName;
        }
        return this.pluralDisplayName;
    }

    public @NotNull String rawDisplayName(@NotNull final BigDecimal value) {
        if (value.equals(BigDecimal.ONE)) {
            return this.rawDisplayName;
        }
        return this.rawPluralDisplayName;
    }

    public @NotNull String rawFormat(@NotNull BigDecimal value) {
        final String formattedAmount = value.setScale(this.decimalPlaces, RoundingMode.HALF_UP)
                .toPlainString();

        return this.format
                .replace("<currency>", this.rawDisplayName(value))
                .replace("<value>", formattedAmount);
    }
}
