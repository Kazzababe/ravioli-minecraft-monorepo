package ravioli.gravioli.common.user.data.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.user.data.User;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class UserCache {
    private final Map<Long, User> cacheById = new ConcurrentHashMap<>();
    private final Map<UUID, User> cacheByUuid = new ConcurrentHashMap<>();
    private final Map<String, User> cacheByUsername = new ConcurrentHashMap<>();
    private final Map<Long, Instant> userExpirationTime = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Duration cacheDuration;

    public UserCache(@NotNull final Duration cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public void invalidate(@NotNull final User user) {
        this.lock.writeLock().lock();

        try {
            this.userExpirationTime.remove(user.id());
            this.cacheById.remove(user.id());
            this.cacheByUuid.remove(user.uuid());
            this.cacheByUsername.remove(user.username());
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void refresh(final long id) {
        this.lock.writeLock().lock();

        try {
            this.userExpirationTime.put(id, Instant.now().plus(this.cacheDuration));
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public void add(@NotNull final User user) {
        this.lock.writeLock().lock();

        try {
            this.userExpirationTime.put(user.id(), Instant.now().plus(this.cacheDuration));
            this.cacheById.put(user.id(), user);
            this.cacheByUuid.put(user.uuid(), user);
            this.cacheByUsername.put(user.username().toLowerCase(), user);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public @Nullable User getById(final long id) {
        User user = null;
        InvalidationState invalidate = InvalidationState.NOTHING;

        this.lock.readLock().lock();

        try {
            final Instant expiration = this.userExpirationTime.get(id);

            if (expiration == null) {
                return null;
            }
            final Instant now = Instant.now();

            user = this.cacheById.get(id);

            if (now.isAfter(expiration)) {
                if (user != null && user.isOnline()) {
                    invalidate = InvalidationState.REFRESH;
                } else {
                    invalidate = InvalidationState.EXPIRED;

                    return null;
                }
            }
            return user;
        } finally {
            this.lock.readLock().unlock();

            if (user != null && invalidate != InvalidationState.NOTHING) {
                if (invalidate == InvalidationState.EXPIRED) {
                    this.invalidate(user);
                } else {
                    this.refresh(user.id());
                }
            }
        }
    }

    public @Nullable User getByUuid(@NotNull final UUID uuid) {
        User user = null;
        InvalidationState invalidate = InvalidationState.NOTHING;

        this.lock.readLock().lock();

        try {
            user = this.cacheByUuid.get(uuid);

            if (user == null) {
                return null;
            }
            final Instant expiration = this.userExpirationTime.get(user.id());

            if (expiration == null) {
                return null;
            }
            final Instant now = Instant.now();

            if (now.isAfter(expiration)) {
                if (user.isOnline()) {
                    invalidate = InvalidationState.REFRESH;
                } else {
                    invalidate = InvalidationState.EXPIRED;

                    return null;
                }
            }
            return user;
        } finally {
            this.lock.readLock().unlock();

            if (user != null && invalidate != InvalidationState.NOTHING) {
                if (invalidate == InvalidationState.EXPIRED) {
                    this.invalidate(user);
                } else {
                    this.refresh(user.id());
                }
            }
        }
    }

    public @Nullable User getByUsername(@NotNull final String username) {
        final String lowerCaseUsername = username.toLowerCase();
        User user = null;
        InvalidationState invalidate = InvalidationState.NOTHING;

        this.lock.readLock().lock();

        try {
            user = this.cacheByUsername.get(lowerCaseUsername);

            if (user == null) {
                return null;
            }
            final Instant expiration = this.userExpirationTime.get(user.id());

            if (expiration == null) {
                return null;
            }
            final Instant now = Instant.now();

            if (now.isAfter(expiration)) {
                if (user.isOnline()) {
                    invalidate = InvalidationState.REFRESH;
                } else {
                    invalidate = InvalidationState.EXPIRED;

                    return null;
                }
            }
            return user;
        } finally {
            this.lock.readLock().unlock();

            if (user != null && invalidate != InvalidationState.NOTHING) {
                if (invalidate == InvalidationState.EXPIRED) {
                    this.invalidate(user);
                } else {
                    this.refresh(user.id());
                }
            }
        }
    }

    private enum InvalidationState {
        NOTHING,
        EXPIRED,
        REFRESH;
    }
}
