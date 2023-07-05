package ravioli.gravioli.auctionhouse.data;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.model.Currency;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;
import ravioli.gravioli.common.data.entity.DataEntity;
import ravioli.gravioli.customitem.item.CustomItem;
import ravioli.gravioli.customitem.service.CustomItemService;
import ravioli.gravioli.mail.item.MailboxItemItemStack;
import ravioli.gravioli.mail.util.MailUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class AuctionHouseItem extends AbstractAuctionHouseItem implements DataEntity {
    private final String displayName;

    public AuctionHouseItem(final long id,
                            final long userId,
                            @NotNull final ItemStack itemStack,
                            @NotNull final Instant createdOn,
                            @NotNull final Instant expiration,
                            @NotNull final Currency currency,
                            @NotNull final BigDecimal cost) {
        super(id, userId, itemStack, createdOn, expiration, currency, cost);

        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null || !itemMeta.hasDisplayName()) {
            this.displayName = WordUtils.capitalizeFully(
                itemStack.getType().name()
                    .toLowerCase()
                    .replace("_", " ")
            );
        } else {
            final Component displayName = Objects.requireNonNull(itemMeta.displayName());
            final StringBuilder stringBuilder = new StringBuilder();

            ComponentFlattener.basic().flatten(displayName, stringBuilder::append);

            this.displayName = stringBuilder.toString();
        }
    }

    public @NotNull String displayName() {
        return this.displayName;
    }

    public @NotNull AuctionHouseItem withId(final long id) {
        return new AuctionHouseItem(id, this.userId(), this.itemStack(), this.createdOn(), this.expiration(), this.currency(), this.cost());
    }

    public @NotNull JsonObject serialize() {
        final JsonObject jsonObject = new JsonObject();
        final JsonObject currencyObject = new JsonObject();

        currencyObject.addProperty("id", this.currency().id());
        currencyObject.addProperty("amount", this.cost().doubleValue());
        jsonObject.addProperty("id", this.id());
        jsonObject.addProperty("user-id", this.userId());
        jsonObject.add("item-stack", this.generateItemStackJson(this.itemStack()));
        jsonObject.addProperty("created-on", this.createdOn().toEpochMilli());
        jsonObject.addProperty("expiration", this.expiration().toEpochMilli());
        jsonObject.add("cost", currencyObject);

        return jsonObject;
    }

    public static @NotNull AuctionHouseItem deserialize(@NotNull final JsonObject serializedData) {
        final JsonObject currencyObject = serializedData.getAsJsonObject("cost");
        final ItemStack itemStack = MailUtil.itemStackFromJson(serializedData.get("item-stack").getAsJsonObject());

        return new AuctionHouseItem(
            serializedData.get("id").getAsLong(),
            serializedData.get("user-id").getAsLong(),
            itemStack,
            Instant.ofEpochMilli(serializedData.get("created-on").getAsLong()),
            Instant.ofEpochMilli(serializedData.get("expiration").getAsLong()),
            Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class))
                .getCurrency(currencyObject.get("id").getAsString())
                .orElseThrow(),
            BigDecimal.valueOf(
                currencyObject.get("amount").getAsDouble()
            )
        );
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("displayName", displayName)
            .append("id", this.id())
            .append("user", this.userId())
            .append("itemStack", this.itemStack().getType().name())
            .append("createdOn", this.createdOn().toString())
            .append("expiration", this.createdOn().toString())
            .append("cost", this.cost().toString())
            .toString();
    }

    private @NotNull JsonObject generateItemStackJson(@NotNull final ItemStack itemStack) {
        final CustomItemService customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));
        final CustomItem<?> customItem = customItemService.getCustomItem(itemStack)
                .orElseThrow();

        return customItem.serialize(itemStack);
    }
}
