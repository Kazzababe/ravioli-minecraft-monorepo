package ravioli.gravioli.core.resourcepack.overlay;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.layout.panel.Panel;

public interface OverlayManager {
    void showPlayer(@NotNull Player player, @NotNull String area, @NotNull OverlayComponent component);

    void hideFromPlayer(@NotNull Player player, @NotNull String componentId);

    void showEveryone(@NotNull String area, @NotNull OverlayComponent component);

    void hideFromEveryone(@NotNull String componentId);

    @NotNull Panel.Builder createRootPanel();
}
