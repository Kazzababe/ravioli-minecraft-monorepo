package ravioli.gravioli.common.currency.transaction;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.user.data.User;

import java.math.BigDecimal;

public record Transaction(@NotNull User user, @NotNull Currency currency, @NotNull BigDecimal amount) {
    public @NotNull Transaction negate() {
        return new Transaction(
            this.user,
            this.currency,
            this.amount.negate()
        );
    }
}
