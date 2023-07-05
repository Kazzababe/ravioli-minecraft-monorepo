package ravioli.gravioli.mail.model.result;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.mail.item.MailboxItem;

import java.util.List;

public record MailCollectionResult(@NotNull List<MailboxItem> items, boolean hasNext) implements CollectionFetchResult<MailboxItem> {
}
