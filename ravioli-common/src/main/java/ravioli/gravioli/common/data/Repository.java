package ravioli.gravioli.common.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.data.cache.CacheHandler;
import ravioli.gravioli.common.data.entity.DataEntity;
import ravioli.gravioli.common.postgres.PostgresProvider;
import ravioli.gravioli.common.redis.RedisProvider;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

public abstract class Repository<T extends DataEntity> {
    private final PostgresProvider postgresProvider;
    private final RedisProvider redisProvider;
    private final CacheHandler<T> cacheHandler;

    @SafeVarargs
    public Repository(@NotNull final PostgresProvider postgresProvider,
            @NotNull final RedisProvider redisProvider,
            @NotNull final Duration expiry,
            @NotNull final Class<? extends T>... entityClasses) {
        this.postgresProvider = postgresProvider;
        this.redisProvider = redisProvider;
        this.cacheHandler = new CacheHandler<>(redisProvider, expiry, entityClasses);
    }

    protected final @NotNull Jedis jedis() {
        return this.redisProvider.getResource();
    }

    protected final @NotNull Connection database() throws SQLException {
        return this.postgresProvider.getConnection();
    }

    protected final void executeSqlFile(@Nullable final InputStream inputStream) throws SQLException, IOException {
        this.postgresProvider.executeScript(inputStream);
    }

    protected final @NotNull CacheHandler<T> cacheHandler() {
        return this.cacheHandler;
    }
}
