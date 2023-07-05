package ravioli.gravioli.common.currency.transfer;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.user.data.User;

import java.math.BigDecimal;

public interface TransferResult {
    /**
     * Returns whether the operations involved in creating the transaction were successful.
     *
     * @return {@code true} if the transaction was a success; {@code false} otherwise.
     */
    boolean success();

    @NotNull Currency currency();

    /**
     * Returns the new balances of the from {@link User} provided the transaction was successful; otherwise this
     * will simply be the {@link CurrencyAmount CurrencyAmounts} from the transaction.
     *
     * @return      The new balances for the from User.
     */
    @NotNull BigDecimal senderBalance();

    /**
     * Returns the new balances of the to {@link User} provided the transaction was successful; otherwise this
     * will simply be the {@link CurrencyAmount CurrencyAmounts} from the transaction.
     *
     * @return      The new balances for the to User.
     */
    @NotNull BigDecimal receiverBalance();
}
