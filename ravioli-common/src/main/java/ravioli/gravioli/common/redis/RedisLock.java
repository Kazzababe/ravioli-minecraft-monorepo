package ravioli.gravioli.common.redis;

import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class RedisLock {
    private final Jedis jedis;
    private final String lockName;
    private final Duration acquireTimeout;
    private final Duration expiry;
    private final String lockValue;

    public RedisLock(@NotNull final Jedis jedis, @NotNull final String lockName) {
        this(jedis, lockName, Duration.ZERO, Duration.ofSeconds(10));
    }

    public RedisLock(
            @NotNull final Jedis jedis, @NotNull final String lockName, @NotNull final Duration acquireTimeout) {
        this(jedis, lockName, acquireTimeout, Duration.ofSeconds(10));
    }

    public RedisLock(
            @NotNull final Jedis jedis,
            @NotNull final String lockName,
            @NotNull final Duration acquireTimeout,
            @NotNull final Duration expiry) {
        this.jedis = jedis;
        this.lockName = lockName;
        this.acquireTimeout = acquireTimeout;
        this.expiry = expiry;
        this.lockValue = UUID.randomUUID().toString();
    }

    /**
     * Attempt to create a lock in redis.
     *
     * @return true if the lock was acquired or false otherwise.
     */
    public boolean lock() {
        long timeout = System.currentTimeMillis() + this.acquireTimeout.toMillis();
        int executions = 0;

        while (System.currentTimeMillis() < timeout || executions++ == 0) {
            if (this.jedis.setnx("lock:" + this.lockName, this.lockValue) == 1) {
                this.jedis.pexpire("lock:" + this.lockName, this.expiry.toMillis());

                return true;
            }
            try {
                Thread.sleep(10);
            } catch (final InterruptedException e) {
                e.printStackTrace();

                return false;
            }
        }
        return false;
    }

    /**
     * Unlock this resource in redis.
     *
     * @return true if the lock was deleted with no modifications.
     */
    public boolean unlock() {
        final String lockKey = "lock:" + this.lockName;

        while (true) {
            try {
                this.jedis.watch(lockKey);

                if (this.lockValue.equals(this.jedis.get(lockKey))) {
                    final Transaction transaction = this.jedis.multi();

                    transaction.del(lockKey);

                    final List<Object> exec = transaction.exec();

                    if (exec == null) {
                        continue;
                    }
                    return true;
                }
                break;
            } catch (final Exception ignored) {

            } finally {
                this.jedis.unwatch();
            }
        }
        return false;
    }
}
