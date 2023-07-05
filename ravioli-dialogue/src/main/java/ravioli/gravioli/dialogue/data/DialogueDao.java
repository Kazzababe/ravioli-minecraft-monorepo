package ravioli.gravioli.dialogue.data;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.postgres.PostgresProvider;
import ravioli.gravioli.dialogue.model.DialogueKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public class DialogueDao {

    private static final String SQL_SET_STATE =
            """
        INSERT INTO dialogue_state (user_id, dialogue, state)
        VALUES (?, ?, ?)
        ON CONFLICT (user_id, dialogue) DO UPDATE SET state = excluded.state
        """;
    private static final String SQL_GET_STATE =
            """
        SELECT state FROM dialogue_state
        WHERE user_id = ? AND dialogue = ?
        """;
    private static final String SQL_SELECT_USER =
            """
        SELECT dialogue, state FROM dialogue_state
        WHERE user_id = ?
        """;

    private final PostgresProvider postgresProvider;

    public DialogueDao(@NotNull final PostgresProvider postgresProvider) {
        this.postgresProvider = postgresProvider;
    }

    public @NotNull CompletableFuture<@NotNull Integer> loadDialogueState(
            @NotNull final UUID uuid, @NotNull final String dialogueId) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection connection = this.postgresProvider.getConnection()) {
                final PreparedStatement statement = connection.prepareStatement(SQL_GET_STATE);

                statement.setObject(1, uuid);
                statement.setString(2, dialogueId);

                final ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            } catch (final SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unable to load dialogue state.", e);

                throw new CompletionException(e);
            }
        });
    }

    public @NotNull CompletableFuture<Void> setDialogueState(
            @NotNull final UUID uuid, @NotNull final String dialogueId, final int state) {
        return CompletableFuture.runAsync(() -> {
            try (final Connection connection = this.postgresProvider.getConnection()) {
                final PreparedStatement statement = connection.prepareStatement(SQL_SET_STATE);

                statement.setObject(1, uuid);
                statement.setString(2, dialogueId);
                statement.setInt(3, state);
                statement.execute();
            } catch (final SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unable to update dialogue state.", e);

                throw new CompletionException(e);
            }
        });
    }

    public @NotNull CompletableFuture<@NotNull Map<DialogueKey, Integer>> loadPlayerStates(@NotNull final UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            final Map<DialogueKey, Integer> stateMap = new HashMap<>();

            try (final Connection connection = this.postgresProvider.getConnection()) {
                final PreparedStatement statement = connection.prepareStatement(SQL_SELECT_USER);

                statement.setObject(1, uuid);

                final ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    final DialogueKey key = new DialogueKey(uuid, resultSet.getString("dialogue"));
                    final int state = resultSet.getInt("state");

                    stateMap.put(key, state);
                }
            } catch (final SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unable to load player dialogue states.", e);

                throw new CompletionException(e);
            }
            return stateMap;
        });
    }
}
