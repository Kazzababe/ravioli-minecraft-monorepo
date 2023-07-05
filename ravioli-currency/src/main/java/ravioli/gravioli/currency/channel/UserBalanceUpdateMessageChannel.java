package ravioli.gravioli.currency.channel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;
import ravioli.gravioli.common.redis.channel.JsonMessageChannel;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;
import ravioli.gravioli.core.util.PluginUtil;
import ravioli.gravioli.currency.event.UserBalanceChangeEvent;

import java.math.BigDecimal;
import java.util.Objects;

public class UserBalanceUpdateMessageChannel extends JsonMessageChannel {
    @Override
    public @NotNull String[] getChannels() {
        return new String[] { "minecraft-event:user-currency-balance-update" };
    }

    @Override
    public void onReceiveMessage(@NotNull final String channel, @NotNull final JsonElement rawData) {
        final JsonObject data = rawData.getAsJsonObject();
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));
        final User user = userService.getById(data.get("userId").getAsLong())
                .orElse(null);

        if (user == null) {
            return;
        }
        PluginUtil.callEvent(new UserBalanceChangeEvent(
            user,
            Objects.requireNonNull(Platform.loadService(CurrencyConfigurationService.class))
                .getCurrency(data.get("currency").getAsString())
                .orElseThrow(),
            BigDecimal.valueOf(
                data.get("balance").getAsDouble()
            )
        ));
    }
}
