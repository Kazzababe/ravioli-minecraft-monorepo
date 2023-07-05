package ravioli.gravioli.common.user;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.user.data.User;

import java.util.UUID;

public abstract class AbstractUser implements User {
    private final long id;
    private final UUID uuid;
    private final String username;

    public AbstractUser(final long id, @NotNull final UUID uuid, @NotNull final String username) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public final long id() {
        return this.id;
    }

    @Override
    public final @NotNull UUID uuid() {
        return this.uuid;
    }

    @Override
    public final @NotNull String username() {
        return this.username;
    }
}
