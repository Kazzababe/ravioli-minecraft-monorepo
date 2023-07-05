package ravioli.gravioli.common.user.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.user.data.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    @NotNull CompletableFuture<@Nullable User> loadByUuid(@NotNull UUID uuid);

    @NotNull CompletableFuture<@Nullable User> loadByUsername(@NotNull String username);

    @NotNull CompletableFuture<@Nullable User> loadById(long id);

    @NotNull CompletableFuture<@NotNull Map<UUID, User>> loadMultipleByUuid(@NotNull Iterable<UUID> uuids);

    @NotNull CompletableFuture<@NotNull Map<String, User>> loadMultipleByUsername(@NotNull Iterable<String> usernames);

    @NotNull CompletableFuture<@NotNull Map<Long, User>> loadMultipleById(@NotNull Iterable<Long> ids);

    @NotNull Optional<@NotNull User> getByUuid(@NotNull UUID uuid);

    @NotNull Optional<@NotNull User> getByUsername(@NotNull String username);

    @NotNull Optional<@NotNull User> getById(long id);

    @NotNull CompletableFuture<@Nullable User> getOrLoadByUuid(@NotNull UUID uuid);

    @NotNull CompletableFuture<@Nullable User> getOrLoadById(long id);

    @NotNull CompletableFuture<@Nullable User> getOrLoadByUsername(@NotNull String username);

    @NotNull CompletableFuture<@NotNull Map<Long, User>> getOrLoadMultipleById(@NotNull Iterable<Long> ids);

    @NotNull CompletableFuture<@NotNull Map<String, User>> getOrLoadMultipleByUsername(@NotNull Iterable<String> usernames);

    @NotNull CompletableFuture<@NotNull Map<UUID, User>> getOrLoadMultipleByUuid(@NotNull Iterable<UUID> uuids);

    @NotNull CompletableFuture<@NotNull User> createUser(@NotNull UUID uuid, @NotNull String username);

    @NotNull CompletableFuture<@NotNull User> updateUsername(@NotNull User user, @NotNull String username);

    void cache(@NotNull User user);

    void invalidate(@NotNull User user);
}
