package ravioli.gravioli.auctionhouse.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.auctionhouse.data.AuctionHouseItem;
import ravioli.gravioli.auctionhouse.filter.AuctionFilter;
import ravioli.gravioli.auctionhouse.model.result.AuctionCollectionResult;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;
import ravioli.gravioli.common.currency.service.CurrencyService;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.http.HttpClientService;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.core.RavioliCorePlugin;
import ravioli.gravioli.core.util.JsonUtil;
import ravioli.gravioli.customitem.item.CustomItem;
import ravioli.gravioli.customitem.service.CustomItemService;
import ravioli.gravioli.mail.item.MailboxItem;
import ravioli.gravioli.mail.item.MailboxItemItemStack;
import ravioli.gravioli.mail.model.MailboxItemCategory;
import ravioli.gravioli.mail.model.MailboxItemType;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.mail.model.result.MailCollectionResult;
import ravioli.gravioli.mail.util.MailUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RestfulAuctionHouseService implements AuctionHouseService {
    @Override
    public @NotNull CompletableFuture<CollectionFetchResult<AuctionHouseItem>> fetchUserRange(@NotNull final User user,
                                                                                              final int start,
                                                                                              final int end,
                                                                                              @NotNull final AuctionFilter filter) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/auction/user/%s/%s/%s/%s/%s"
            .formatted(
                user.id(),
                filter.sortDirection().name(),
                filter.sortType().name(),
                start,
                end
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final List<AuctionHouseItem> items = new ArrayList<>();
                final JsonArray itemsArray = response.getAsJsonArray("items");

                for (final JsonElement jsonElement : itemsArray) {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final AuctionHouseItem auctionHouseItem = this.parse(jsonObject);

                    items.add(auctionHouseItem);
                }
                return new AuctionCollectionResult(items, response.get("next").getAsBoolean());
            });
    }

    @Override
    public @NotNull CompletableFuture<CollectionFetchResult<AuctionHouseItem>> fetchGlobalRange(final int start,
                                                                                                final int end,
                                                                                                @NotNull final AuctionFilter filter) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/auction/global/%s/%s/%s/%s"
            .formatted(
                filter.sortDirection().name(),
                filter.sortType().name(),
                start,
                end
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final List<AuctionHouseItem> items = new ArrayList<>();
                final JsonArray itemsArray = response.getAsJsonArray("items");

                for (final JsonElement jsonElement : itemsArray) {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final AuctionHouseItem auctionHouseItem = this.parse(jsonObject);

                    items.add(auctionHouseItem);
                }
                return new AuctionCollectionResult(items, response.get("next").getAsBoolean());
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull AuctionHouseItem> create(@NotNull final AuctionHouseItem auctionHouseItem) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/auction/create";
        final JsonObject body = new JsonObject();

        body.addProperty("userId", auctionHouseItem.userId());
        body.addProperty("expiration", auctionHouseItem.expiration().toString());
        body.addProperty("currency", auctionHouseItem.currency().id());
        body.addProperty("cost", auctionHouseItem.cost().doubleValue());
        body.addProperty("displayName", auctionHouseItem.displayName());
        body.add("item", this.generateItemStackJson(auctionHouseItem.itemStack()));

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final long id = response.get("data").getAsLong();

                return auctionHouseItem.withId(id);
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull final AuctionHouseItem... auctionHouseItems) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/auction/delete";
        final JsonObject body = new JsonObject();
        final JsonArray idsArray = new JsonArray();

        for (final AuctionHouseItem item : auctionHouseItems) {
            idsArray.add(item.id());
        }
        body.add("ids", idsArray);

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return true;
            });
    }

    private @NotNull AuctionHouseItem parse(@NotNull final JsonObject jsonObject) {
        final long id = jsonObject.get("id").getAsLong();
        final long userId = jsonObject.get("userId").getAsLong();
        final Instant createdAt = Instant.parse(jsonObject.get("createdOn").getAsString());
        final Instant expiration = Instant.parse(jsonObject.get("expiration").getAsString());
        final BigDecimal cost = BigDecimal.valueOf(jsonObject.get("cost").getAsDouble());
        final Currency currency = Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class))
            .getCurrency(jsonObject.get("currency").getAsString())
            .orElseThrow();

        return new AuctionHouseItem(
            id,
            userId,
            MailUtil.itemStackFromJson(jsonObject.getAsJsonObject("item")),
            createdAt,
            expiration,
            currency,
            cost
        );
    }

    private @NotNull JsonObject generateItemStackJson(@NotNull final ItemStack itemStack) {
        final CustomItemService customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));
        final CustomItem<?> customItem = customItemService.getCustomItem(itemStack)
                .orElseThrow();

        return customItem.serialize(itemStack);
    }
}
