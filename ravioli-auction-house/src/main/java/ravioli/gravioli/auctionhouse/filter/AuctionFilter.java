package ravioli.gravioli.auctionhouse.filter;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.model.SortDirection;
import ravioli.gravioli.auctionhouse.model.SortType;

public record AuctionFilter(@NotNull SortDirection sortDirection, @NotNull SortType sortType) {
}
