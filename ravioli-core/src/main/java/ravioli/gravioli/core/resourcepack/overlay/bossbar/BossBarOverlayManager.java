package ravioli.gravioli.core.resourcepack.overlay.bossbar;

import net.kyori.adventure.bossbar.BossBar;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BossBarOverlayManager implements OverlayManager {
    private static final BossBarOverlayManager OVERLAY_MANAGER = new BossBarOverlayManager();

    public static @NotNull OverlayManager get() {
        return OVERLAY_MANAGER;
    }

    static {
        PluginUtil.executeWhenEnabled(OVERLAY_MANAGER::enable);
    }
    private final Map<UUID, PlayerOverlayController> playerOverlayControllers;
    private final Map<UUID, BossBar> playerBossBars;

    public BossBarOverlayManager() {
        this.playerOverlayControllers = new ConcurrentHashMap<>();
        this.playerBossBars = new ConcurrentHashMap<>();
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
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
    }

    private synchronized void updatePlayer(@NotNull final Player player, @NotNull final ResourcePackComponent resourcePackComponent) {
        final Component component = new CenteredEnvironmentPanel.Builder()
            .alignment(Alignment.CENTER)
            .width(500)
            .child(resourcePackComponent)
            .create();
        final UUID uuid = player.getUniqueId();
        BossBar bossBar = this.playerBossBars.get(uuid);

        if (bossBar != null) {
            bossBar.name(component);

            return;
        }
        bossBar = BossBar.bossBar(component, 0, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);

        player.showBossBar(bossBar);
        this.playerBossBars.put(uuid, bossBar);
    }

    private void clearPlayer(@NotNull final Player player) {
        final UUID uuid = player.getUniqueId();
        final BossBar bossBar = this.playerBossBars.remove(uuid);

        this.playerOverlayControllers.remove(uuid);

        if (bossBar != null) {
            player.hideBossBar(bossBar);
        }
    }

    private class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerQuit(final PlayerQuitEvent event) {
            final UUID uuid = event.getPlayer().getUniqueId();

            playerOverlayControllers.remove(uuid);
            playerBossBars.remove(uuid);
        }
    }
}
