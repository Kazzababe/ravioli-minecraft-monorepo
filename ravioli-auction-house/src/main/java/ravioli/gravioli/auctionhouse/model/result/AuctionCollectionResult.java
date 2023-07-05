package ravioli.gravioli.auctionhouse.model.result;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.data.AuctionHouseItem;
import ravioli.gravioli.common.data.result.CollectionFetchResult;

import java.util.List;

public record AuctionCollectionResult(@NotNull List<AuctionHouseItem> items, boolean hasNext) implements CollectionFetchResult<AuctionHouseItem> {

}