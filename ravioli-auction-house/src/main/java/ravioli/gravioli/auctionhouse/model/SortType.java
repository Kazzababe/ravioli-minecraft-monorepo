package ravioli.gravioli.auctionhouse.model;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.data.AuctionHouseItem;

import java.util.Comparator;

public enum SortType {
    CREATED_AT ("Date Posted", Comparator.comparing(AuctionHouseItem::id)),
    ALPHABETICAL ("Alphabetical", Comparator.comparing((item) -> item.itemStack().getType().name())),
    COST ("Price", Comparator.comparing((item) -> item.cost().doubleValue()));

    private final String displayName;
    private final Comparator<AuctionHouseItem> comparator;

    SortType(@NotNull final String displayName, @NotNull final Comparator<AuctionHouseItem> comparator) {
        this.displayName = displayName;
        this.comparator = comparator;
    }

    public @NotNull String displayName() {
        return this.displayName;
    }

    public @NotNull Comparator<AuctionHouseItem> comparator() {
        return this.comparator;
    }
}
