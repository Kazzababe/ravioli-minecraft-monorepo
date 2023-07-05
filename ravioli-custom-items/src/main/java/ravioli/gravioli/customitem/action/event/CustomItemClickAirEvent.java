package ravioli.gravioli.customitem.action.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomItemClickAirEvent implements CustomItemEvent {
    private final PlayerInteractEvent bukkitEvent;

    public CustomItemClickAirEvent(@NotNull final PlayerInteractEvent event) {
        this.bukkitEvent = event;
    }

    public @NotNull Player player() {
        return this.bukkitEvent.getPlayer();
    }

    public @Nullable EquipmentSlot hand() {
        return this.bukkitEvent.getHand();
    }

    public @NotNull Action action() {
        return this.bukkitEvent.getAction();
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

    public void setCancelled(final boolean cancelled) {
        this.bukkitEvent.setCancelled(cancelled);
    }
}
