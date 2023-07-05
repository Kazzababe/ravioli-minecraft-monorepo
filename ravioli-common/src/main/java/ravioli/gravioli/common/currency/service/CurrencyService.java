package ravioli.gravioli.common.currency.service;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.transaction.Transaction;
import ravioli.gravioli.common.currency.transaction.TransactionResult;
import ravioli.gravioli.common.currency.transfer.TransferResult;
import ravioli.gravioli.common.user.data.User;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface CurrencyService {

    @NotNull CompletableFuture<@NotNull BigDecimal> load(@NotNull User user,
                                                         @NotNull Currency currency);

    @NotNull CompletableFuture<@NotNull TransactionResult> deposit(@NotNull User user,
                                                                   @NotNull Transaction transaction);

    @NotNull CompletableFuture<@NotNull TransactionResult> withdraw(@NotNull User user,
                                                                    @NotNull Transaction transaction);

    @NotNull CompletableFuture<@NotNull TransferResult> transfer(@NotNull User from,
                                                                 @NotNull User to,
                                                                 @NotNull Transaction transaction);
}
