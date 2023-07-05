package ravioli.gravioli.common.user.data;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.postgres.PostgresProvider;
import ravioli.gravioli.common.user.AbstractUser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class.getSimpleName());
    
    private static final Function<Integer, String> SQL_LOAD_ID = (amount) -> """
        SELECT * FROM ravioli_user WHERE id IN (%s)
        """
        .formatted(
            IntStream.range(0, amount)
                .mapToObj((number) -> "?")
                .collect(Collectors.joining(", "))
        );
    private static final Function<Integer, String> SQL_LOAD_UUID = (amount) -> """
        SELECT * FROM ravioli_user WHERE uuid IN (%s)
        """
        .formatted(
            IntStream.range(0, amount)
                .mapToObj((number) -> "?")
                .collect(Collectors.joining(", "))
        );
    private static final Function<Integer, String> SQL_LOAD_USERNAME = (amount) -> """
        SELECT * FROM ravioli_user WHERE username IN (%s)
        """
        .formatted(
            IntStream.range(0, amount)
                .mapToObj((number) -> "?")
                .collect(Collectors.joining(", "))
        );
    private static final String SQL_CREATE = """
        INSERT INTO ravioli_user (uuid, username) VALUES (?, ?)
        """;
    private static final String SQL_UPDATE_USERNAME = """
        UPDATE ravioli_user
        SET username = ?
        WHERE id = ?
        """;

    private final PostgresProvider postgresProvider;

    public UserDao(@NotNull final PostgresProvider postgresProvider) {
        this.postgresProvider = postgresProvider;
    }

    public @NotNull Map<Long, User> loadById(@NotNull final List<Long> ids) {
        try (final Connection connection = this.postgresProvider.getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(SQL_LOAD_ID.apply(ids.size()));

            for (int i = 0; i < ids.size(); i++) {
                statement.setLong(i + 1, ids.get(i));
            }
            final ResultSet resultSet = statement.executeQuery();
            final Map<Long, User> users = new LinkedHashMap<>();

            while (resultSet.next()) {
                final User user = this.newUser(
                    resultSet.getLong("id"),
                    UUID.fromString(
                        resultSet.getString("uuid")
                    ),
                    resultSet.getString("username")
                );

                users.put(user.id(), user);
            }
            return users;
        } catch (final SQLException e) {
            LOGGER.error("Unable to load user by id.");

            throw new RuntimeException(e);
        }
    }

    public @NotNull Map<UUID, User> loadByUuid(@NotNull final List<UUID> uuids) {

        try (final Connection connection = this.postgresProvider.getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(SQL_LOAD_UUID.apply(uuids.size()));

            for (int i = 0; i < uuids.size(); i++) {
                statement.setObject(i + 1, uuids.get(i));
            }
            final ResultSet resultSet = statement.executeQuery();
            final Map<UUID, User> users = new HashMap<>();

            while (resultSet.next()) {
                final User user = this.newUser(
                    resultSet.getLong("id"),
                    UUID.fromString(
                        resultSet.getString("uuid")
                    ),
                    resultSet.getString("username")
                );

                users.put(user.uuid(), user);
            }
            return users;
        } catch (final SQLException e) {
            LOGGER.error("Unable to load user by uuid.");

            throw new RuntimeException(e);
        }
    }

    public @NotNull Map<String, User> loadByUsername(@NotNull final List<String> usernames,
                                                     final boolean caseSensitive) {
        try (final Connection connection = this.postgresProvider.getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(SQL_LOAD_USERNAME.apply(usernames.size()));

            for (int i = 0; i < usernames.size(); i++) {
                statement.setString(i + 1, usernames.get(i));
            }
            final ResultSet resultSet = statement.executeQuery();
            final Map<String, User> users = new HashMap<>();

            while (resultSet.next()) {
                final String username = resultSet.getString("username");
                final User user = this.newUser(
                    resultSet.getLong("id"),
                    UUID.fromString(
                        resultSet.getString("uuid")
                    ),
                    username
                );

                users.put(caseSensitive ? username : username.toLowerCase(), user);
            }
            return users;
        } catch (final SQLException e) {
            LOGGER.error("Unable to load user by username.");

            throw new RuntimeException(e);
        }
    }

    public @NotNull User create(@NotNull final UUID uuid, @NotNull final String username) {
        try (final Connection connection = this.postgresProvider.getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(SQL_CREATE, Statement.RETURN_GENERATED_KEYS);

            statement.setObject(1, uuid);
            statement.setString(2, username);
            statement.execute();

            final ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                return this.newUser(resultSet.getLong(1), uuid, username);
            }
            throw new SQLException("No primary id was generated after inserting a this.newUser.");
        } catch (final SQLException e) {
            LOGGER.error("Unable to create user.", e);

            throw new RuntimeException(e);
        }
    }

    public @NotNull User updateUsername(@NotNull final User user, @NotNull final String username) {

        try (final Connection connection = this.postgresProvider.getConnection()) {
            final PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_USERNAME);

            statement.setLong(1, user.id());
            statement.setString(2, username);
            statement.execute();

            return user.withUsername(username);
        } catch (final SQLException e) {
            LOGGER.error("Unable to create user.", e);

            throw new RuntimeException(e);
        }
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
}
