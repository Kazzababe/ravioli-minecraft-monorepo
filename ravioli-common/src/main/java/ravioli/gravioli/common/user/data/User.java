package ravioli.gravioli.common.user.data;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface User {
    long id();

    @NotNull UUID uuid();

    @NotNull String username();

    boolean isOnline();

    @NotNull User withUsername(@NotNull final String username);
}
