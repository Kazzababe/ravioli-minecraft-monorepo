package ravioli.gravioli.mail.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.http.HttpClientService;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.core.util.JsonUtil;
import ravioli.gravioli.mail.item.MailboxItem;
import ravioli.gravioli.mail.item.MailboxItemItemStack;
import ravioli.gravioli.mail.model.MailboxItemCategory;
import ravioli.gravioli.mail.model.MailboxItemType;
import ravioli.gravioli.mail.model.MailboxType;
import ravioli.gravioli.mail.model.result.MailCollectionResult;
import ravioli.gravioli.mail.util.MailUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RestfulMailService implements MailService {
    @Override
    public @NotNull CompletableFuture<@NotNull MailboxItem> create(@NotNull MailboxItem mailboxItem) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/mail/create";
        final JsonObject body = new JsonObject();

        body.addProperty("userId", mailboxItem.userId());
        body.addProperty("mailboxType", mailboxItem.mailboxType().name());
        body.addProperty("mailboxItemCategory", mailboxItem.category().name());
        body.addProperty("groupId", "all");
        body.addProperty("title", mailboxItem.title());
        body.addProperty("message", mailboxItem.message());
        body.addProperty("type", mailboxItem.type().name());
        body.add("data", mailboxItem.data());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final long newId = response.get("data").getAsLong();

                return mailboxItem.withId(newId);
            });
    }

    @Override
    public @NotNull CompletableFuture<CollectionFetchResult<MailboxItem>> fetchRange(@NotNull final User user,
                                                                                     @NotNull final MailboxType mailboxType,
                                                                                     final int start,
                                                                                     final int end) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/mail/%s/%s/%s/%s"
            .formatted(
                user.id(),
                start,
                end,
                mailboxType.name()
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final List<MailboxItem> items = new ArrayList<>();
                final JsonArray itemsArray = response.getAsJsonArray("items");

                for (final JsonElement jsonElement : itemsArray) {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final MailboxItem mailboxItem = this.parse(jsonObject);

                    items.add(mailboxItem);
                }
                return new MailCollectionResult(items, response.get("next").getAsBoolean());
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull final MailboxItem mailboxItem) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/mail/delete";
        final JsonObject body = new JsonObject();

        body.addProperty("id", mailboxItem.id());
        body.addProperty("mailboxType", mailboxItem.mailboxType().name());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return true;
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Boolean> setOpened(@NotNull final MailboxItem mailboxItem,
                                                                  final boolean opened) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/mail/set-opened";
        final JsonObject body = new JsonObject();

        body.addProperty("id", mailboxItem.id());
        body.addProperty("mailboxType", mailboxItem.mailboxType().name());
        body.addProperty("opened", opened);

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return true;
            });
    }

    private @NotNull MailboxItem parse(@NotNull final JsonObject jsonObject) {
        final long id = jsonObject.get("id").getAsLong();
        final String typeString = jsonObject.get("type").getAsString();
        final MailboxItemType mailboxItemType = MailboxItemType.valueOf(typeString);
        final String title = JsonUtil.getString(jsonObject, "title");
        final String message = JsonUtil.getString(jsonObject, "message");
        final Instant createdAt = Instant.parse(jsonObject.get("createdOn").getAsString());
        final Instant updatedAt = Instant.parse(jsonObject.get("updatedOn").getAsString());
        final boolean opened = jsonObject.get("opened").getAsBoolean();

        return switch (mailboxItemType) {
            case ITEM -> new MailboxItemItemStack(
                id,
                jsonObject.get("userId").getAsLong(),
                MailboxType.valueOf(jsonObject.get("mailboxType").getAsString()),
                MailboxItemCategory.valueOf(jsonObject.get("mailboxItemCategory").getAsString()),
                title,
                message,
                createdAt,
                updatedAt,
                opened,
                MailUtil.itemStacksFromJson(jsonObject.getAsJsonArray("items"))
            );
        };
    }
}
