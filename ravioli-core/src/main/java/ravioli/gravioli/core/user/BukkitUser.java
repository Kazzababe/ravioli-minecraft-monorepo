package ravioli.gravioli.core.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.user.AbstractUser;
import ravioli.gravioli.common.user.data.User;

import java.util.Objects;
import java.util.UUID;

public final class BukkitUser extends AbstractUser {
    public BukkitUser(final long id, @NotNull final UUID uuid, @NotNull final String username) {
        super(id, uuid, username);
    }

    @Override
    public @NotNull BukkitUser withUsername(@NotNull final String username) {
        return new BukkitUser(this.id(), this.uuid(), username);
    }

    public @Nullable Player asBukkitPlayer() {
        return Bukkit.getPlayer(this.uuid());
    }

    public boolean isOnline() {
        return this.asBukkitPlayer() != null;
    }

    public boolean is(@NotNull final Player player) {
        return this.uuid().equals(player.getUniqueId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", this.id())
            .append("uuid", this.uuid())
            .append("username", this.username())
            .toString();
    }

    @Override
    public boolean equals(@Nullable final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final User that = (User) other;

        if (this.id() != that.id()) {
            return false;
        }
        return this.uuid().equals(that.uuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id(), this.uuid());
    }
}
