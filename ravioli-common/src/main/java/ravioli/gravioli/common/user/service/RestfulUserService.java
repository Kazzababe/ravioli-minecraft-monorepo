package ravioli.gravioli.common.user.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.transaction.CurrencyTransactionResult;
import ravioli.gravioli.common.http.HttpClientService;
import ravioli.gravioli.common.user.AbstractUser;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.data.cache.UserCache;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RestfulUserService implements UserService {
    private final UserCache userCache;

    public RestfulUserService() {
        this.userCache = new UserCache(Duration.ofMinutes(5));
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> loadByUuid(@NotNull final UUID uuid) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/uuid/%s"
            .formatted(uuid.toString());

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonElement rawData = response.get("data");

                if (rawData.isJsonNull()) {
                    return null;
                }
                final JsonObject data = rawData.getAsJsonObject();

                return this.newUser(
                    data.get("id").getAsLong(),
                    uuid,
                    data.get("username").getAsString()
                );
            })
            .thenApply((user) -> {
                if (user != null) {
                    this.userCache.add(user);
                }
                return user;
            });
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> loadByUsername(@NotNull final String username) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/username/%s"
            .formatted(username);

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonElement rawData = response.get("data");

                if (rawData.isJsonNull()) {
                    return null;
                }
                final JsonObject data = rawData.getAsJsonObject();

                return this.newUser(
                    data.get("id").getAsLong(),
                    UUID.fromString(
                        data.get("uuid").getAsString()
                    ),
                    data.get("username").getAsString()
                );
            })
            .thenApply((user) -> {
                if (user != null) {
                    this.userCache.add(user);
                }
                return user;
            });
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> loadById(final long id) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/id/%s"
            .formatted(id);

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonElement rawData = response.get("data");

                if (rawData.isJsonNull()) {
                    return null;
                }
                final JsonObject data = rawData.getAsJsonObject();

                return this.newUser(
                    data.get("id").getAsLong(),
                    UUID.fromString(
                        data.get("uuid").getAsString()
                    ),
                    data.get("username").getAsString()
                );
            })
            .thenApply((user) -> {
                if (user != null) {
                    this.userCache.add(user);
                }
                return user;
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<UUID, User>> loadMultipleByUuid(@NotNull final Iterable<UUID> uuids) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/uuids/%s"
            .formatted(
                this.listFromIterator(uuids)
                    .stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","))
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonElement rawData = response.get("data");

                if (rawData.isJsonNull()) {
                    return Collections.emptyMap();
                }
                final JsonObject data = rawData.getAsJsonObject();
                final Map<UUID, User> users = new HashMap<>();

                for (final String uuidKey : data.keySet()) {
                    final UUID uuid = UUID.fromString(uuidKey);
                    final JsonObject userObject = data.getAsJsonObject(uuidKey);

                    users.put(
                        uuid,
                        this.newUser(
                            userObject.get("id").getAsLong(),
                            uuid,
                            userObject.get("username").getAsString()
                        )
                    );
                }
                for (final User user : users.values()) {
                    this.userCache.add(user);
                }
                return users;
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<String, User>> loadMultipleByUsername(@NotNull final Iterable<String> usernames) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/usernames/%s"
            .formatted(
                String.join(",", this.listFromIterator(usernames))
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonElement rawData = response.get("data");

                if (rawData.isJsonNull()) {
                    return Collections.emptyMap();
                }
                final JsonObject data = rawData.getAsJsonObject();
                final Map<String, User> users = new HashMap<>();

                for (final String usernameKey : data.keySet()) {
                    final JsonObject userObject = data.getAsJsonObject(usernameKey);

                    users.put(
                        usernameKey,
                        this.newUser(
                            userObject.get("id").getAsLong(),
                            UUID.fromString(
                                userObject.get("uuid").getAsString()
                            ),
                            usernameKey
                        )
                    );
                }
                for (final User user : users.values()) {
                    this.userCache.add(user);
                }
                return users;
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<Long, User>> loadMultipleById(@NotNull final Iterable<Long> ids) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/ids/%s"
            .formatted(
                this.listFromIterator(ids)
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","))
            );

        return httpService.get(url, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonElement rawData = response.get("data");

                if (rawData.isJsonNull()) {
                    return Collections.emptyMap();
                }
                final JsonObject data = rawData.getAsJsonObject();
                final Map<Long, User> users = new HashMap<>();

                for (final String idKey : data.keySet()) {
                    final long id = Long.parseLong(idKey);
                    final JsonObject userObject = data.getAsJsonObject(idKey);

                    users.put(
                        id,
                        this.newUser(
                            id,
                            UUID.fromString(
                                userObject.get("uuid").getAsString()
                            ),
                            userObject.get("username").getAsString()
                        )
                    );
                }
                for (final User user : users.values()) {
                    this.userCache.add(user);
                }
                return users;
            });
    }

    @Override
    public @NotNull Optional<@NotNull User> getByUuid(@NotNull final UUID uuid) {
        return Optional.ofNullable(this.userCache.getByUuid(uuid));
    }

    @Override
    public @NotNull Optional<@NotNull User> getByUsername(@NotNull final String username) {
        return Optional.ofNullable(this.userCache.getByUsername(username));
    }

    @Override
    public @NotNull Optional<@NotNull User> getById(final long id) {
        return Optional.ofNullable(this.userCache.getById(id));
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> getOrLoadByUuid(@NotNull final UUID uuid) {
        return this.getByUuid(uuid)
            .map(CompletableFuture::completedFuture)
            .orElse(this.loadByUuid(uuid));
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> getOrLoadById(final long id) {
        return this.getById(id)
            .map(CompletableFuture::completedFuture)
            .orElse(this.loadById(id));
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> getOrLoadByUsername(@NotNull final String username) {
        return this.getByUsername(username)
            .map(CompletableFuture::completedFuture)
            .orElse(this.loadByUsername(username));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<Long, User>> getOrLoadMultipleById(@NotNull final Iterable<Long> ids) {
        return CompletableFuture.supplyAsync(() -> {
            final Map<Long, User> users = new HashMap<>();
            final Set<Long> missingIds = new HashSet<>();

            for (final long id : ids) {
                final User user = this.userCache.getById(id);

                if (user == null) {
                    missingIds.add(id);

                    continue;
                }
                users.put(id, user);
            }
            if (missingIds.isEmpty()) {
                return users;
            }
            try {
                final Map<Long, User> loadedUsers = loadMultipleById(missingIds).get();

                users.putAll(loadedUsers);
            } catch (final InterruptedException | ExecutionException e) {
                throw new CompletionException(e);
            }
            return users;
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<String, User>> getOrLoadMultipleByUsername(@NotNull Iterable<String> usernames) {
        return CompletableFuture.supplyAsync(() -> {
            final Map<String, User> users = new HashMap<>();
            final Set<String> missingUsernames = new HashSet<>();

            for (final String username : usernames) {
                final User user = this.userCache.getByUsername(username);

                if (user == null) {
                    missingUsernames.add(username);

                    continue;
                }
                users.put(username, user);
            }
            if (missingUsernames.isEmpty()) {
                return users;
            }
            try {
                final Map<String, User> loadedUsers = loadMultipleByUsername(missingUsernames).get();

                users.putAll(loadedUsers);
            } catch (final InterruptedException | ExecutionException e) {
                throw new CompletionException(e);
            }
            return users;
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<UUID, User>> getOrLoadMultipleByUuid(@NotNull final Iterable<UUID> uuids) {
        return CompletableFuture.supplyAsync(() -> {
            final Map<UUID, User> users = new HashMap<>();
            final Set<UUID> missingUuids = new HashSet<>();

            for (final UUID uuid : uuids) {
                final User user = this.userCache.getByUuid(uuid);

                if (user == null) {
                    missingUuids.add(uuid);

                    continue;
                }
                users.put(uuid, user);
            }
            if (missingUuids.isEmpty()) {
                return users;
            }
            try {
                final Map<UUID, User> loadedUsers = loadMultipleByUuid(missingUuids).get();

                users.putAll(loadedUsers);
            } catch (final InterruptedException | ExecutionException e) {
                throw new CompletionException(e);
            }
            return users;
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull User> createUser(@NotNull final UUID uuid, @NotNull final String username) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/create";
        final JsonObject body = new JsonObject();

        body.addProperty("username", username);
        body.addProperty("uuid", uuid.toString());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                final JsonObject data = response.getAsJsonObject("data");

                return this.newUser(
                    data.get("id").getAsLong(),
                    uuid,
                    username
                );
            });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull User> updateUsername(@NotNull final User user, @NotNull final String username) {
        final HttpClientService httpService = Objects.requireNonNull(Platform.loadService(HttpClientService.class));
        final String url = Platform.API_BASE_URL + "api/user/update-username";
        final JsonObject body = new JsonObject();

        body.addProperty("username", username);
        body.addProperty("id", user.id());

        return httpService.post(url, body, Collections.emptyMap())
            .thenApply((response) -> {
                if (response == null) {
                    throw new RuntimeException();
                }
                return user.withUsername(username);
            });
    }

    @Override
    public void cache(@NotNull final User user) {
        this.userCache.add(user);
    }

    @Override
    public void invalidate(@NotNull final User user) {
        this.userCache.invalidate(user);
    }

    private @NotNull User newUser(final long id, @NotNull final UUID uuid, @NotNull final String username) {
        final Class<? extends AbstractUser> userClass = Platform.getUserClass();

        try {
            final Constructor<? extends AbstractUser> constructor = userClass.getDeclaredConstructor(long.class, UUID.class, String.class);

            return constructor.newInstance(id, uuid, username);
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException |
                       InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull <T> List<T> listFromIterator(@NotNull final Iterable<T> iterable) {
        final List<T> list = new ArrayList<>();

        for (final T item : iterable) {
            list.add(item);
        }
        return list;
    }
}
