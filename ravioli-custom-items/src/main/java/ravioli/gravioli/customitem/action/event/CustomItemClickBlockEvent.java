package ravioli.gravioli.customitem.action.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CustomItemClickBlockEvent implements CustomItemEvent {
    private final PlayerInteractEvent bukkitEvent;

    public CustomItemClickBlockEvent(@NotNull final PlayerInteractEvent event) {
        this.bukkitEvent = event;
    }

    public @NotNull Player player() {
        return this.bukkitEvent.getPlayer();
    }

    public @NotNull Action action() {
        return this.bukkitEvent.getAction();
    }

    public @Nullable EquipmentSlot hand() {
        return this.bukkitEvent.getHand();
    }

    public @NotNull Block clickedBlock() {
        return Objects.requireNonNull(this.bukkitEvent.getClickedBlock());
    }

    public @NotNull BlockFace clickedBlockFace() {
        return this.bukkitEvent.getBlockFace();
    }

    public @Nullable Location interactionPoint() {
        return this.bukkitEvent.getInteractionPoint();
    }

    public void useItemInHand(final boolean allow) {
        this.bukkitEvent.setUseItemInHand(allow ? Event.Result.ALLOW : Event.Result.DENY);
    }

    public boolean useItemInHand() {
        return switch (this.bukkitEvent.useItemInHand()) {
            case DENY -> false;
            case DEFAULT, ALLOW -> true;
        };
    }

    public void useClickedBlock(final boolean allow) {
        this.bukkitEvent.setUseInteractedBlock(allow ? Event.Result.ALLOW : Event.Result.DENY);
    }

    public boolean useClockedBlock() {
        return switch (this.bukkitEvent.useInteractedBlock()) {
            case DENY -> false;
            case DEFAULT, ALLOW -> true;
        };
    }

    public void setCancelled(final boolean cancelled) {
        this.bukkitEvent.setCancelled(cancelled);
    }
}
