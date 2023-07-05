package ravioli.gravioli.common.currency.transfer;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;

import java.math.BigDecimal;

public record CurrencyTransferResult(boolean success,
                                     @NotNull Currency currency,
                                     @NotNull BigDecimal senderBalance,
                                     @NotNull BigDecimal receiverBalance) implements TransferResult {
}
