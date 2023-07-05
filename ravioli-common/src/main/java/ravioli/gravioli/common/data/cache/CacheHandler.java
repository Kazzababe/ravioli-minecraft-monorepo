package ravioli.gravioli.common.data.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.data.entity.DataEntity;
import ravioli.gravioli.common.data.result.CollectionFetchResult;
import ravioli.gravioli.common.redis.RedisProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * CacheHandler provides cache handling functionalities with Redis for entities.
 * It allows caching, invalidating, and retrieving individual entities and collections of entities.
 *
 * @param <T> Type of the entity to be handled. It must be a subclass of {@link DataEntity}
 */
public class CacheHandler<T extends DataEntity> {
    private final RedisProvider redisProvider;
    private final Map<String, Method> deserializationMethods = new HashMap<>();
    private final String keyPrefix;
    private final long expiry;

    /**
     * Constructor for CacheHandler.
     *
     * @param redisProvider   the provider for Redis connections
     * @param expiry          duration after which the cache expires
     * @param entityClasses   classes of entities to be handled by this cache
     */
    @SafeVarargs
    public CacheHandler(@NotNull final RedisProvider redisProvider, @NotNull final Duration expiry,
                        @NotNull final Class<? extends T>... entityClasses) {
        if (entityClasses.length == 0) {
            throw new RuntimeException("No entity classes supplied to cache handler.");
        }
        for (final Class<? extends T> entityClass : entityClasses) {
            try {
                final Method deserializationMethod = entityClass.getDeclaredMethod("deserialize", JsonObject.class);

                if (!deserializationMethod.getReturnType().equals(entityClass)) {
                    throw new RuntimeException("Entity deserialization method does not return the correct type.");
                }
                this.deserializationMethods.put(entityClass.getSimpleName().toLowerCase(), deserializationMethod);
            } catch (final NoSuchMethodException e) {
                throw new RuntimeException("Entity class has no static method \"deserialize\".");
            }
        }
        this.redisProvider = redisProvider;
        this.keyPrefix = entityClasses[0].getSimpleName();
        this.expiry = expiry.toMillis();
    }

    private @NotNull Jedis jedis() {
        return this.redisProvider.getResource();
    }

    /**
     * Provides the prefix key used for caching entities.
     * This is typically the simple name of the first entity class provided at the creation of this handler.
     *
     * @return the prefix key for the cache
     */
    public final @NotNull String keyPrefix() {
        return this.keyPrefix;
    }

    /**
     * Invalidates the cache for the provided keys.
     *
     * @param keys    keys of the cache entries to invalidate
     */
    public final void invalidateKeys(@NotNull final Object... keys) {
        try (final Jedis jedis = this.jedis()) {
            final Pipeline pipeline = jedis.pipelined();

            for (final Object key : keys) {
                pipeline.del(this.keyPrefix + ":" + key);
            }
            pipeline.sync();
        }
    }

    /**
     * Invalidates the cache for a given collection.
     *
     * @param collectionId    ID of the collection to invalidate
     */
    public void invalidateCollection(@NotNull final String collectionId) {
        final String redisPrefixKey = this.keyPrefix + ":" + collectionId;

        try (final Jedis jedis = this.jedis()) {
            // This could technically have a race condition (cause bad cache results)
            final Set<String> members = jedis.smembers(redisPrefixKey);

            if (members.isEmpty()) {
                return;
            }
            final Pipeline pipeline = jedis.pipelined();

            pipeline.del(members.toArray(String[]::new));
            pipeline.del(redisPrefixKey);
            pipeline.sync();
        }
    }

    /**
     * Caches a given entity.
     *
     * @param key         key of the entity
     * @param dataEntity  the entity to cache
     */
    public final void cache(@NotNull final Object key, @NotNull final T dataEntity) {
        final String className = dataEntity.getClass().getSimpleName().toLowerCase();
        final JsonObject data = dataEntity.serialize();

        data.addProperty("entity-class", className);

        try (final Jedis jedis = this.jedis()) {
            jedis.psetex(this.keyPrefix + ":" + key, this.expiry, data.toString());
        }
    }

    /**
     * Caches a collection of entities.
     *
     * @param collectionId    ID of the collection
     * @param key             key of the collection
     * @param dataEntities    entities to cache
     */
    public final void cacheCollection(@NotNull final String collectionId, @NotNull final Object key,
                                      @NotNull final Collection<T> dataEntities) {
        this.cacheCollection(collectionId, key, dataEntities, false);
    }

    /**
     * Caches a collection of entities.
     *
     * @param collectionId    ID of the collection
     * @param key             key of the collection
     * @param dataEntities    entities to cache
     * @param hasNext         whether the collection has more entities
     */
    public void cacheCollection(@NotNull final String collectionId, @NotNull final Object key,
                                @NotNull final Collection<T> dataEntities, final boolean hasNext) {
        final JsonObject jsonObject = new JsonObject();
        final JsonArray dataJson = new JsonArray();

        for (final T dataEntity : dataEntities) {
            final JsonObject data = dataEntity.serialize();
            final String className = dataEntity.getClass().getSimpleName().toLowerCase();

            data.addProperty("entity-class", className);
            dataJson.add(data.toString());
        }
        jsonObject.add("items", dataJson);
        jsonObject.addProperty("hasNext", hasNext);

        try (final Jedis jedis = this.jedis()) {
            final String redisPrefixKey = this.keyPrefix + ":" + collectionId;
            final String redisKey = redisPrefixKey + ":" + key;

            jedis.psetex(redisKey, this.expiry, jsonObject.toString());
            jedis.sadd(redisPrefixKey, redisKey);
        }
    }

    /**
     * Retrieves a cached entity. If the entity is not in the cache, it retrieves the entity using the provided function.
     *
     * @param key             key of the entity
     * @param mapFunction     function to retrieve the entity if it's not in the cache
     * @return the retrieved entity
     */
    public final @NotNull T getOr(@NotNull final Object key,
                                  @NotNull final Supplier<T> mapFunction) {
        try (final Jedis jedis = this.jedis()) {
            final String data = jedis.get(this.keyPrefix + ":" + key);

            if (data == null) {
                return mapFunction.get();
            }
            final JsonObject jsonData = JsonParser.parseString(data).getAsJsonObject();
            final String entityClassName = jsonData.get("entity-class").getAsString();
            final Method deserializationMethod = this.deserializationMethods.get(entityClassName);

            if (deserializationMethod == null) {
                throw new RuntimeException("No deserialization method found for data entity.");
            }
            return (T) deserializationMethod.invoke(null, jsonData);
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a cached collection of entities as a {@link CollectionFetchResult}.
     * If the collection is not in the cache, it retrieves the collection using the provided function.
     *
     * @param collectionId    ID of the collection
     * @param key             key of the collection
     * @param mapFunction     function to retrieve the collection if it's not in the cache
     * @return the retrieved collection
     */
    public final @NotNull CollectionFetchResult<T> getCollectionResultOr(@NotNull final String collectionId,
                                                                         @NotNull final Object key,
                                                                         @NotNull final Supplier<CollectionFetchResult<T>> mapFunction) {
        try (final Jedis jedis = this.jedis()) {
            final String redisPrefixKey = this.keyPrefix + ":" + collectionId;
            final String data = jedis.get(redisPrefixKey + ":" + key);

            if (data == null) {
                return mapFunction.get();
            }
            final JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            final JsonArray dataJson = jsonObject.getAsJsonArray("items");
            final boolean hasNext = jsonObject.get("hasNext").getAsBoolean();
            final List<T> allData = new ArrayList<>();

            for (final JsonElement dataElement : dataJson) {
                final String serializedData = dataElement.getAsString();
                final JsonObject jsonSerializedData = JsonParser.parseString(serializedData).getAsJsonObject();
                final String entityClassName = jsonSerializedData.get("entity-class").getAsString();
                final Method deserializationMethod = this.deserializationMethods.get(entityClassName);

                if (deserializationMethod == null) {
                    throw new RuntimeException("No deserialization method found for data entity.");
                }
                allData.add((T) deserializationMethod.invoke(null, jsonSerializedData));
            }
            return new CollectionFetchResult<>() {
                @Override
                public @NotNull List<T> items() {
                    return allData;
                }

                @Override
                public boolean hasNext() {
                    return hasNext;
                }
            };
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a cached collection of entities.
     * If the collection is not in the cache, it retrieves the collection using the provided function.
     *
     * @param collectionId    ID of the collection
     * @param key             key of the collection
     * @param mapFunction     function to retrieve the collection if it's not in the cache
     * @return the retrieved collection
     */
    public final @NotNull Collection<@NotNull T> getCollectionOr(@NotNull final String collectionId,
                                                                @NotNull final Object key,
                                                                @NotNull final Supplier<Collection<T>> mapFunction) {
        try (final Jedis jedis = this.jedis()) {
            final String redisPrefixKey = this.keyPrefix + ":" + collectionId;
            final String data = jedis.get(redisPrefixKey + ":" + key);

            if (data == null) {
                return mapFunction.get();
            }
            final JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            final JsonArray dataJson = jsonObject.getAsJsonArray("items");
            final List<T> allData = new ArrayList<>();

            for (final JsonElement dataElement : dataJson) {
                final String serializedData = dataElement.getAsString();
                final JsonObject jsonSerializedData = JsonParser.parseString(serializedData).getAsJsonObject();
                final String entityClassName = jsonSerializedData.get("entity-class").getAsString();
                final Method deserializationMethod = this.deserializationMethods.get(entityClassName);

                if (deserializationMethod == null) {
                    throw new RuntimeException("No deserialization method found for data entity.");
                }
                allData.add((T) deserializationMethod.invoke(null, jsonSerializedData));
            }
            return allData;
        } catch (final InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
