package ravioli.gravioli.mail.item;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.mail.model.MailboxItemCategory;
import ravioli.gravioli.mail.model.MailboxType;

import java.time.Instant;

public abstract class AbstractMailboxItem implements MailboxItem {

    protected final long id;

    protected final long userId;
    protected final MailboxType mailboxType;
    protected final MailboxItemCategory mailboxItemCategory;
    protected final String message;
    protected final String title;
    protected final Instant createdOn;
    protected final Instant updatedOn;
    protected final boolean opened;

    public AbstractMailboxItem(@NotNull final User user,
                               @NotNull final MailboxType mailboxType,
                               @NotNull final MailboxItemCategory mailboxItemCategory,
                               @Nullable final String title,
                               @Nullable final String message) {
        this(-1, user.id(), mailboxType, mailboxItemCategory, title, message, Instant.now(), Instant.now(), false);
    }

    AbstractMailboxItem(final long id,
                        @NotNull final User user,
                        @NotNull final MailboxType mailboxType,
                        @NotNull final MailboxItemCategory category,
                        @Nullable final String title,
                        @Nullable final String message,
                        @NotNull Instant createdOn,
                        @NotNull Instant updatedOn,
                        final boolean opened) {
        this(id, user.id(), mailboxType, category, title, message, createdOn, updatedOn, opened);
    }

    AbstractMailboxItem(final long id,
                        final long userId,
                        @NotNull final MailboxType mailboxType,
                        @NotNull final MailboxItemCategory category,
                        @Nullable final String title,
                        @Nullable final String message,
                        @NotNull final Instant createdOn,
                        @NotNull final Instant updatedOn,
                        final boolean opened) {
        this.id = id;
        this.mailboxType = mailboxType;
        this.mailboxItemCategory = category;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.opened = opened;
    }

    public long id() {
        return this.id;
    }

    public long userId() {
        return this.userId;
    }

    public @NotNull MailboxType mailboxType() {
        return this.mailboxType;
    }

    public @Nullable String title() {
        return this.title;
    }

    public @NotNull MailboxItemCategory category() {
        return this.mailboxItemCategory;
    }

    public @Nullable String message() {
        return this.message;
    }

    public @NotNull Instant createdOn() {
        return this.createdOn;
    }

    public @NotNull Instant updatedOn() {
        return this.updatedOn;
    }

    public boolean opened() {
        return this.opened;
    }

    protected final @NotNull JsonObject generateBaseJson() {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("user-id", this.userId);
        jsonObject.addProperty("id", this.id);
        jsonObject.addProperty("mailbox-type", this.mailboxType.name());
        jsonObject.addProperty("mailbox-category", this.mailboxItemCategory.name());
        jsonObject.addProperty("opened", this.opened);

        if (this.message != null) {
            jsonObject.addProperty("message", this.message);
        }
        if (this.title != null) {
            jsonObject.addProperty("title", this.title);
        }
        jsonObject.addProperty("created-on", this.createdOn.toEpochMilli());
        jsonObject.addProperty("updated-on", this.updatedOn.toEpochMilli());

        return jsonObject;
    }
}
