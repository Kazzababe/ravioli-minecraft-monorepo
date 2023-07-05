package ravioli.gravioli.mail.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.data.entity.DataEntity;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.mail.model.MailboxItemCategory;
import ravioli.gravioli.mail.model.MailboxItemType;
import ravioli.gravioli.mail.model.MailboxType;

import java.time.Instant;

public interface MailboxItem extends DataEntity {
    /**
     * Return the id of the {@link MailboxItem}.
     *
     * @return      The id of the item.
     */
    long id();

    /**
     * Return the id of the {@link User} that the {@link MailboxItem} belongs to.
     *
     * @return      The id the owning User.
     */
    long userId();

    /**
     * Return the {@link MailboxType} of the {@link MailboxItem}. The MailboxType determines how MailboxItems are
     * grouped together in their respective UIs.
     *
     * @return      The MailboxType of the item.
     */
    @NotNull MailboxType mailboxType();

    /**
     * Return the subject line of the {@link MailboxItem}.
     *
     * @return      The title of the item.
     */
    @Nullable String title();

    /**
     * Return the {@link MailboxItemCategory} of the {@link MailboxItem}. This is the primary category that the
     * individual mail entity falls under.
     *
     * @return      The category of the item.
     */
    @NotNull MailboxItemCategory category();

    /**
     * Return a message to be included with the {@link MailboxItem}. A {@code null} return value indiciates that
     * the mail item has no message.
     *
     * @return      The optional text body of the item.
     */
    @Nullable String message();

    /**
     * Return the date the {@link MailboxItem} was created on.
     *
     * @return      The date the item was created on.
     */
    @NotNull Instant createdOn();

    /**
     * Return the date the {@link MailboxItem} was last modified.
     *
     * @return      The date the item was last modified.
     */
    @NotNull Instant updatedOn();

    boolean opened();

    /**
     * The type of item this {@link MailboxItem} is.
     *
     * @return      The item type.
     */
    @NotNull MailboxItemType type();

    /**
     * Return a clone of the {@link MailboxItem} with a new id.
     *
     * @param id        The new id.
     * @return          The new MailboxItem.
     */
    @NotNull MailboxItem withId(long id);

    /**
     * A JSON representation of the entire {@link MailboxItem} that will be used for caching.
     *
     * @return      The MailboxItem entity as a {@link JsonObject}.
     */
    @NotNull JsonObject toJson();

    /**
     * The data that represents this specific {@link MailboxItem}.
     MailboxItem
     * @return      A {@link JsonElement} representing the item.
     */
    @NotNull JsonElement data();

    /**
     * Return the {@link ItemStack} that will represent this item in the mailbox UI.
     *
     * @return      An ItemStack.
     */
    @NotNull ItemStack icon();

    /**
     * Give the online {@link Player} the contents of the {@link MailboxItem}. The returned {@link ClaimResult}
     * determines how the mail interface will handle a {@link User} attempting to claim the item.
     *
     * @param player        The Player claiming the item.
     * @return              The result of the claim attempt.
     */
    @NotNull ClaimResult claim(@NotNull Player player);

    enum ClaimResult {
        CLAIMED,
        FAILED,
        IGNORE;
    }
}
