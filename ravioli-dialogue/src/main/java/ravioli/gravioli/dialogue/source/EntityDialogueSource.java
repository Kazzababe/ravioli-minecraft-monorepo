package ravioli.gravioli.dialogue.source;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EntityDialogueSource implements DialogueSource {
    private static final double DEFAULT_DISTANCE_THRESHOLD = 3;

    private final Entity entity;
    private final double distanceThreshold;

    public EntityDialogueSource(@NotNull final Entity entity, final double distanceThreshold) {
        this.entity = entity;
        this.distanceThreshold = distanceThreshold * distanceThreshold;
    }

    public EntityDialogueSource(@NotNull final Entity entity) {
        this(entity, DEFAULT_DISTANCE_THRESHOLD);
    }

    @Override
    public boolean isValid(@NotNull Player player) {
        final Location entityLocation = this.entity.getLocation();
        final Location playerLocation = player.getLocation();

        if (!entityLocation.getWorld().equals(playerLocation.getWorld())) {
            return false;
        }
        return entityLocation.distanceSquared(playerLocation) <= this.distanceThreshold;
    }
}
