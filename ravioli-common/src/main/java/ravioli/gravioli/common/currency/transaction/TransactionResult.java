package ravioli.gravioli.common.currency.transaction;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;

import java.math.BigDecimal;

public interface TransactionResult {
    /**
     * Returns whether the operations involved in creating the transaction were successful.
     *
     * @return {@code true} if the transaction was a success; {@code false} otherwise.
     */
    boolean success();

    @NotNull Currency currency();

    /**
     * Returns the amount being transferred as a {@link BigDecimal}.
     * TODO: UPDATE
     *
     * @return The amount of currency being transferred.
     */
    @NotNull BigDecimal amount();

    /**
     * Returns the new balance of the user after the transaction.
     * TODO: UPDATE
     *
     * @return The user's new balance.
     */
    @NotNull BigDecimal balance();
}
