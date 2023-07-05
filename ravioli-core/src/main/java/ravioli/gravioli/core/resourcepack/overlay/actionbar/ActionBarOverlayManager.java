package ravioli.gravioli.core.resourcepack.overlay.actionbar;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.panel.CenteredEnvironmentPanel;
import ravioli.gravioli.core.resourcepack.layout.panel.Panel;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;
import ravioli.gravioli.core.resourcepack.overlay.OverlayComponent;
import ravioli.gravioli.core.resourcepack.overlay.OverlayManager;
import ravioli.gravioli.core.resourcepack.overlay.player.PlayerOverlayController;
import ravioli.gravioli.core.util.PluginUtil;
import ravioli.gravioli.core.util.SchedulerUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionBarOverlayManager implements OverlayManager, Runnable {
    private static final ActionBarOverlayManager OVERLAY_MANAGER = new ActionBarOverlayManager();

    public static @NotNull OverlayManager get() {
        return OVERLAY_MANAGER;
    }

    static {
        PluginUtil.executeWhenEnabled(OVERLAY_MANAGER::enable);
    }

    private final Map<UUID, PlayerOverlayController> playerOverlayControllers;
    private final Map<UUID, Component> latestPlayerComponents;

    public ActionBarOverlayManager() {
        this.playerOverlayControllers = new ConcurrentHashMap<>();
        this.latestPlayerComponents = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void showPlayer(@NotNull final Player player, @NotNull final String area,
                                        @NotNull final OverlayComponent overlayComponent) {
        final UUID uuid = player.getUniqueId();
        final PlayerOverlayController controller = this.playerOverlayControllers
            .computeIfAbsent(
                uuid,
                (key) -> new PlayerOverlayController(
                    this,
                    uuid,
                    (component) -> this.updatePlayer(player, component),
                    () -> this.clearPlayer(player)
                ));

        controller.add(area, overlayComponent);
    }

    @Override
    public synchronized void hideFromPlayer(@NotNull final Player player, @NotNull final String componentId) {
        final PlayerOverlayController controller = this.playerOverlayControllers.get(player.getUniqueId());

        if (controller == null) {
            return;
        }
        controller.remove(componentId);
    }

    @Override
    public synchronized void showEveryone(@NotNull final String area, @NotNull final OverlayComponent component) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.showPlayer(player, area, component);
        }
    }

    @Override
    public synchronized void hideFromEveryone(@NotNull final String componentId) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.hideFromPlayer(player, componentId);
        }
    }

    @Override
    public @NotNull Panel.Builder createRootPanel() {
        return new Panel.Builder(500)
            .alignment(Alignment.CENTER);
    }

    private void enable(@NotNull final Plugin plugin) {
        SchedulerUtil.async().runInterval(this, 5, 5);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
    }

    private synchronized void updatePlayer(@NotNull final Player player, @NotNull final ResourcePackComponent resourcePackComponent) {
        final Component component = new CenteredEnvironmentPanel.Builder()
            .alignment(Alignment.CENTER)
            .width(500)
            .child(resourcePackComponent)
            .create();

        this.latestPlayerComponents.put(player.getUniqueId(), component);
        player.sendActionBar(component);
    }

    private synchronized void clearPlayer(@NotNull final Player player) {
        final UUID uuid = player.getUniqueId();

        if (player.isOnline()) {
            player.sendActionBar(Component.empty());
        }
        this.latestPlayerComponents.remove(uuid);
        this.playerOverlayControllers.remove(uuid);
    }

    @Override
    public synchronized void run() {
        for (final Map.Entry<UUID, Component> entry : this.latestPlayerComponents.entrySet()) {
            final Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null) {
                continue;
            }
            player.sendActionBar(entry.getValue());
        }
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerQuit(final PlayerQuitEvent event) {
            clearPlayer(event.getPlayer());
        }
    }
}
