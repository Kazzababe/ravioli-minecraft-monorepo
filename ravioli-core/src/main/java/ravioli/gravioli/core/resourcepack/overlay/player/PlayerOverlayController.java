package ravioli.gravioli.core.resourcepack.overlay.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.panel.Panel;
import ravioli.gravioli.core.resourcepack.overlay.OverlayComponent;
import ravioli.gravioli.core.resourcepack.overlay.OverlayManager;
import ravioli.gravioli.core.util.SchedulerUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PlayerOverlayController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerOverlayController.class);

    private final Map<String, OverlayComponentEntry> components;
    private final OverlayManager overlayManager;
    private final UUID playerId;
    private final Consumer<ResourcePackComponent> updateFunction;
    private final Runnable emptyOverlayFunction;

    public PlayerOverlayController(@NotNull final OverlayManager overlayManager,
                                   @NotNull final UUID playerId,
                                   @NotNull final Consumer<ResourcePackComponent> updateFunction,
                                   @NotNull final Runnable emptyOverlayFunction) {
        this.components = new HashMap<>();
        this.overlayManager = overlayManager;
        this.playerId = playerId;
        this.updateFunction = updateFunction;
        this.emptyOverlayFunction = emptyOverlayFunction;
    }

    public synchronized void add(@NotNull final String area, @NotNull final OverlayComponent component) {
        this.components.put(component.id(), new OverlayComponentEntry(component, area));

        this.triggerUpdate();
    }

    public synchronized void remove(@NotNull final String componentId) {
        this.components.remove(componentId);

        this.triggerUpdate();
    }

    private void triggerUpdate() {
        this.createComponent()
            .thenAcceptAsync((component) -> {
                if (component == null) {
                    this.emptyOverlayFunction.run();

                    return;
                }
                this.updateFunction.accept(component);
            }, SchedulerUtil.sync().executor())
            .exceptionally((e) -> {
                LOGGER.error("Unable to create overlay component:", e);

                return null;
            });
    }

    private @NotNull CompletableFuture<@Nullable ResourcePackComponent> createComponent() {
        final Set<String> areas = this.components.values()
            .stream()
            .map((OverlayComponentEntry::area))
            .collect(Collectors.toSet());
        final Panel.Builder panelBuilder = this.overlayManager.createRootPanel();
        final Player player = Bukkit.getPlayer(this.playerId);

        if (player == null) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            boolean addedChildren = false;

            for (final String area : areas) {
                final OverlayComponent bestComponent = this.findHighestPriorityComponent(area);

                if (bestComponent == null) {
                    continue;
                }
                final ResourcePackComponent component = bestComponent.component(player);

                if (component == null) {
                    continue;
                }
                panelBuilder.child(component);

                addedChildren = true;
            }
            if (!addedChildren) {
                return null;
            }
            return panelBuilder.create();
        });
    }

    private @Nullable OverlayComponent findHighestPriorityComponent(@NotNull final String area) {
        return this.components.values()
            .stream()
            .filter((entry) -> entry.area.equals(area))
            .map(OverlayComponentEntry::component)
            .max(Comparator.comparingDouble(OverlayComponent::priority))
            .orElse(null);
    }

    private record OverlayComponentEntry(@NotNull OverlayComponent component, @NotNull String area) {

    }
}
