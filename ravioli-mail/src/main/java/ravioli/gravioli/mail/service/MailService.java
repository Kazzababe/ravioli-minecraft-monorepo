package ravioli.gravioli.mail.service;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.mail.item.MailboxItem;
import ravioli.gravioli.mail.model.MailboxType;

import java.util.concurrent.CompletableFuture;

public interface MailService {
    @NotNull CompletableFuture<@NotNull MailboxItem> create(@NotNull MailboxItem mailboxItem);

    @NotNull CompletableFuture<CollectionFetchResult<MailboxItem>> fetchRange(@NotNull User user,
                                                                              @NotNull MailboxType mailboxType,
                                                                              int start,
                                                                              int end);

    @NotNull CompletableFuture<@NotNull Boolean> delete(@NotNull MailboxItem mailboxItem);

    @NotNull CompletableFuture<@NotNull Boolean> setOpened(@NotNull MailboxItem mailboxItem, boolean opened);
}
