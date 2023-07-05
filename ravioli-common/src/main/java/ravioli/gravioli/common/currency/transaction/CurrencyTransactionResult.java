package ravioli.gravioli.common.currency.transaction;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;

import java.math.BigDecimal;

public record CurrencyTransactionResult(boolean success,
                                        @NotNull Currency currency,
                                        @NotNull BigDecimal amount,
                                        @NotNull BigDecimal balance) implements TransactionResult {
}
