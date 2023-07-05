package ravioli.gravioli.common;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.currency.service.CurrencyService;
import ravioli.gravioli.common.currency.service.RestfulCurrencyService;
import ravioli.gravioli.common.http.HttpClientService;
import ravioli.gravioli.common.http.OkHttpClientService;
import ravioli.gravioli.common.postgres.PostgresProvider;
import ravioli.gravioli.common.redis.RedisProvider;
import ravioli.gravioli.common.user.AbstractUser;
import ravioli.gravioli.common.user.service.RestfulUserService;
import ravioli.gravioli.common.user.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Platform {
    public static final String API_BASE_URL = Objects.requireNonNull(System.getenv("API_BASE_URL"), "Missing \"API_BASE_URL\" env variable.");
    public static final String EVENT_RECEIVER_URL = Objects.requireNonNull(System.getenv("EVENT_RECEIVER_URL"), "Missing \"EVENT_RECEIVER_URL\" env variable.");

    private static final Map<Class<?>, Object> REGISTERED_SERVICES = new HashMap<>();

    private static Class<? extends AbstractUser> USER_CLASS;
    private static RedisProvider REDIS_PROVIDER;
    private static PostgresProvider POSTGRES_PROVIDER;

    static {
        registerService(CurrencyService.class, new RestfulCurrencyService());
        registerService(HttpClientService.class, new OkHttpClientService());
        registerService(UserService.class, new RestfulUserService());
    }

    public static void setUserClass(@NotNull final Class<? extends AbstractUser> userClass) {
        if (USER_CLASS != null) {
            throw new IllegalStateException("USER_CLASS can only be set once!");
        }
        USER_CLASS = userClass;
    }

    public static @NotNull Class<? extends AbstractUser> getUserClass() {
        return Objects.requireNonNull(USER_CLASS, "No USER_CLASS was set!");
    }

    public static void setDefaultRedisProvider(@NotNull final RedisProvider redisProvider) {
        if (REDIS_PROVIDER != null) {
            throw new IllegalStateException("REDIS_PROVIDER can only be set once!");
        }
        REDIS_PROVIDER = redisProvider;
    }

    public static @NotNull RedisProvider getDefaultRedisProvider() {
        return Objects.requireNonNull(REDIS_PROVIDER, "No REDIS_PROVIDER was set!");
    }

    public static void setDefaultPostgresProvider(@NotNull final PostgresProvider postgresProvider) {
        if (POSTGRES_PROVIDER != null) {
            throw new IllegalStateException("POSTGRES_PROVIDER can only be set once!");
        }
        POSTGRES_PROVIDER = postgresProvider;
    }

    public static @NotNull PostgresProvider getDefaultPostgresProvider() {
        return Objects.requireNonNull(POSTGRES_PROVIDER, "No POSTGRES_PROVIDER was set!");
    }

    public static <T> void registerService(@NotNull final Class<T> serviceClass, @NotNull final T serviceImplementation) {
        REGISTERED_SERVICES.put(serviceClass, serviceImplementation);
    }

    public static @NotNull <T> T loadService(@NotNull final Class<T> serviceClass) {
        return (T) REGISTERED_SERVICES.get(serviceClass);
    }
}
