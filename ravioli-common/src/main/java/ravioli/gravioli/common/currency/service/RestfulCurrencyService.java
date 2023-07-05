package ravioli.gravioli.common.currency.service;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.transaction.CurrencyTransactionResult;
import ravioli.gravioli.common.currency.transaction.Transaction;
import ravioli.gravioli.common.currency.transaction.TransactionResult;
import ravioli.gravioli.common.currency.transfer.CurrencyTransferResult;
import ravioli.gravioli.common.currency.transfer.TransferResult;
import ravioli.gravioli.common.http.HttpClientService;
import ravioli.gravioli.common.user.data.User;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RestfulCurrencyService implements CurrencyService {
    @Override
    public @NotNull CompletableFuture<@NotNull BigDecimal> load(@NotNull final User user,
                                                                @NotNull final Currency currency) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/currency/%s/%s"
            .formatted(
                user.id(),
                currency.id()
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final double balance = response.get("data").getAsDouble();

                return BigDecimal.valueOf(balance);
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull TransactionResult> deposit(@NotNull final User user,
                                                                          @NotNull final Transaction transaction) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/currency/update";
        final JsonObject body = new JsonObject();

        body.addProperty("userId", user.id());
        body.addProperty("currency", transaction.currency().id());
        body.addProperty("amount", transaction.amount());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return new CurrencyTransactionResult(
                    response.get("success").getAsBoolean(),
                    transaction.currency(),
                    transaction.amount(),
                    BigDecimal.valueOf(
                        response.get("balance").getAsDouble()
                    )
                );
            })
            .thenApply((response) -> {
                if (response.success()) {
//                    PluginUtil.callEvent(new UserBalanceChangeEvent(user, response.currency(), response.balance()));
                }
                return response;
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull TransactionResult> withdraw(@NotNull final User user,
                                                                           @NotNull final Transaction transaction) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/currency/update";
        final JsonObject body = new JsonObject();

        body.addProperty("userId", user.id());
        body.addProperty("currency", transaction.currency().id());
        body.addProperty("amount", transaction.negate().amount());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return new CurrencyTransactionResult(
                    response.get("success").getAsBoolean(),
                    transaction.currency(),
                    transaction.amount(),
                    BigDecimal.valueOf(
                        response.get("balance").getAsDouble()
                    )
                );
            })
            .thenApply((response) -> {
                if (response.success()) {
//                    PluginUtil.callEvent(new UserBalanceChangeEvent(user, response.currency(), response.balance()));
                }
                return response;
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull TransferResult> transfer(@NotNull final User from,
                                                                        @NotNull final User to,
                                                                        @NotNull final Transaction transaction) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/currency/transfer";
        final JsonObject body = new JsonObject();

        body.addProperty("senderId", from.id());
        body.addProperty("receiverId", to.id());
        body.addProperty("currency", transaction.currency().id());
        body.addProperty("amount", transaction.amount());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return new CurrencyTransferResult(
                    response.get("success").getAsBoolean(),
                    transaction.currency(),
                    BigDecimal.valueOf(
                        response.get("senderBalance").getAsDouble()
                    ),
                    BigDecimal.valueOf(
                        response.get("receiverBalance").getAsDouble()
                    )
                );
            })
            .thenApply((response) -> {
                if (response.success()) {
//                    PluginUtil.callEvent(new UserBalanceChangeEvent(from, response.currency(), response.senderBalance()));
//                    PluginUtil.callEvent(new UserBalanceChangeEvent(to, response.currency(), response.receiverBalance()));
                }
                return response;
            });
    }
}
