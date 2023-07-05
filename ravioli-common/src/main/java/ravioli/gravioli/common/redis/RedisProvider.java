package ravioli.gravioli.common.redis;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.redis.channel.MessageChannel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public interface RedisProvider {
    /**
     * Return a new Jedis object outside the {@link JedisPool}.
     *
     * @return a new jedis instance created outside a JedisPool
     */
    @NotNull Jedis newInstance();

    /**
     * Return a new jedis object from the {@link JedisPool}.
     *
     * @return a new jedis instance created from a JedisPool
     */
    @NotNull Jedis getResource();

    /**
     * Close the redis connection pool.
     */
    void close();

    /**
     * Register a redis message channel instance.
     *
     * @param messageChannel    The message channel to register
     */
    void registerChannel(@NotNull MessageChannel<?> messageChannel);
}
