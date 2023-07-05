package ravioli.gravioli.currency.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.user.data.User;

import java.math.BigDecimal;

public class UserBalanceChangeEvent extends Event {
    public static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    private final User user;
    private final Currency currency;
    private final BigDecimal balance;

    public UserBalanceChangeEvent(@NotNull final User user, @NotNull final Currency currency,
                                  @NotNull final BigDecimal balance) {
        super(true);

        this.user = user;
        this.currency = currency;
        this.balance = balance;
    }

    public @NotNull User getUser() {
        return this.user;
    }

    public @NotNull Currency getCurrency() {
        return this.currency;
    }

    public @NotNull BigDecimal getBalance() {
        return this.balance;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
