package ravioli.gravioli.core.resourcepack.overlay;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;

public interface OverlayComponent {
    int priority();

    @NotNull String id();

    @Nullable ResourcePackComponent component(@NotNull Player player);
}
