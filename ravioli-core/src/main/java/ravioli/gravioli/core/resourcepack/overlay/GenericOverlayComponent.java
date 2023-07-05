package ravioli.gravioli.core.resourcepack.overlay;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;

public record GenericOverlayComponent(int priority, @NotNull String id, @NotNull ResourcePackComponent component) implements OverlayComponent {

    @Override
    public @NotNull ResourcePackComponent component(@NotNull final Player _player) {
        return this.component;
    }
}
