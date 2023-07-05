package ravioli.gravioli.common.redis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.redis.channel.MessageChannel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.time.Duration;
import java.util.Map;

public class BasicRedisProvider implements RedisProvider {
    private final JedisPool jedisPool;

    private final String host;
    private final int port;
    private final String password;

    public BasicRedisProvider(@NotNull final String host, final int port, @Nullable final String password,
                              @NotNull final Map<String, String> options) {
        this.host = host;
        this.port = port;
        this.password = password == null || password.isBlank() ? null : password;

        final JedisPoolConfig jedisConfig = new JedisPoolConfig();

        jedisConfig.setMaxWait(Duration.ofSeconds(
                Integer.parseInt(options.getOrDefault("maxWaitTime", "15"))
        ));
        jedisConfig.setMaxTotal(Integer.parseInt(options.getOrDefault("maxTotalTime", "125")));
        jedisConfig.setMaxIdle(Integer.parseInt(options.getOrDefault("maxIdleTime", "125")));
        jedisConfig.setMinIdle(Integer.parseInt(options.getOrDefault("minIdleTime", "16")));

        if (this.password != null) {
            this.jedisPool = new JedisPool(jedisConfig, this.host, this.port, Protocol.DEFAULT_TIMEOUT, this.password);
        } else {
            this.jedisPool = new JedisPool(jedisConfig, this.host, this.port);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Jedis newInstance() {
        final Jedis jedis = new Jedis(this.host, this.port);

        if (this.password != null) {
            jedis.auth(this.password);
        }
        return jedis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Jedis getResource() {
        return this.jedisPool.getResource();
    }

    @Override
    public void close() {
        this.jedisPool.close();
    }

    @Override
    public void registerChannel(@NotNull final MessageChannel<?> messageChannel) {
        // TODO: This is weird
        messageChannel.init(this);
    }
}
