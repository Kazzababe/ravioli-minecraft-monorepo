package ravioli.gravioli.common.redis.channel;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.redis.RedisProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public abstract class JsonMessageChannel extends JedisPubSub implements MessageChannel<JsonElement> {
    @Override
    public void init(@NotNull final RedisProvider redisProvider) {
        final Thread thread = new Thread(() -> {
            try (final Jedis jedis = redisProvider.newInstance()) {
                jedis.subscribe(this, this.getChannels());
            }
        });

        thread.setDaemon(true);
        thread.setName("JsonMessageChannel:" + this.getClass().getSimpleName());
        thread.start();
    }

    @Override
    public void onMessage(@NotNull final String channel, @NotNull final String message) {
        final JsonElement jsonObject = JsonParser.parseString(message);

        this.onReceiveMessage(channel, jsonObject);
    }
}
