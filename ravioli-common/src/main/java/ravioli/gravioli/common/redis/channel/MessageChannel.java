package ravioli.gravioli.common.redis.channel;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.redis.RedisProvider;

public interface MessageChannel<T> {
    @NotNull String[] getChannels();

    void onReceiveMessage(@NotNull String channel, @NotNull T message);

    void init(@NotNull RedisProvider redisProvider);
}
