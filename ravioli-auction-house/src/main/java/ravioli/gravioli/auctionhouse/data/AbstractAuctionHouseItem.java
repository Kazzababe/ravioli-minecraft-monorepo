package ravioli.gravioli.auctionhouse.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public abstract class AbstractAuctionHouseItem {
    private final long id;
    private final long userId;
    private final ItemStack itemStack;
    private final Instant createdOn;
    private final Instant expiration;
    private final Currency currency;
    private final BigDecimal cost;

    public AbstractAuctionHouseItem(final long id,
                                    final long userId,
                                    @NotNull final ItemStack itemStack,
                                    @NotNull final Instant createdOn,
                                    @NotNull final Instant expiration,
                                    @NotNull final Currency currency,
                                    @NotNull final BigDecimal cost) {
        this.id = id;
        this.userId = userId;
        this.itemStack = itemStack;
        this.createdOn = createdOn;
        this.expiration = expiration;
        this.currency = currency;
        this.cost = cost;
    }

    public long id() {
        return this.id;
    }

    public long userId() {
        return this.userId;
    }

    public @NotNull ItemStack itemStack() {
        return this.itemStack.clone();
    }

    public @NotNull Instant createdOn() {
        return this.createdOn;
    }

    public @NotNull Instant expiration() {
        return this.expiration;
    }

    public @NotNull Currency currency() {
        return this.currency;
    }

    public @NotNull BigDecimal cost() {
        return this.cost;
    }

    @Blocking
    public @NotNull User user() {
        return Objects.requireNonNull(
            Objects.requireNonNull(Platform.loadService(UserService.class))
                        .getOrLoadById(this.userId)
                        .join()
        );
    }
}
