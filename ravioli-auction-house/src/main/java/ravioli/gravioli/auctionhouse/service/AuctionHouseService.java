package ravioli.gravioli.auctionhouse.service;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.data.AuctionHouseItem;
import ravioli.gravioli.auctionhouse.filter.AuctionFilter;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.user.data.User;

import java.util.concurrent.CompletableFuture;

public interface AuctionHouseService {
    @NotNull CompletableFuture<CollectionFetchResult<AuctionHouseItem>> fetchUserRange(@NotNull User user,
                                                                                       int start,
                                                                                       int end,
                                                                                       @NotNull AuctionFilter filter);


    @NotNull CompletableFuture<CollectionFetchResult<AuctionHouseItem>> fetchGlobalRange(int start,
                                                                                         int end,
                                                                                         @NotNull AuctionFilter filter);

    @NotNull CompletableFuture<@NotNull AuctionHouseItem> create(@NotNull AuctionHouseItem auctionHouseItem);

    @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull AuctionHouseItem... auctionHouseItems);
}
