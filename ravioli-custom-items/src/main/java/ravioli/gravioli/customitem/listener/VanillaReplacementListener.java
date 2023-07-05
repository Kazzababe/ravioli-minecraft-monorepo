package ravioli.gravioli.customitem.listener;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.customitem.item.CustomItem;
import ravioli.gravioli.customitem.service.CustomItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class VanillaReplacementListener implements Listener {
    private final CustomItemService customItemService;

    public VanillaReplacementListener() {
        this.customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockDropItem(final BlockDropItemEvent event) {
        for (final Item item : event.getItems()) {
            this.handle(item.getItemStack(), item::setItemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onLootGenerate(final LootGenerateEvent event) {
        final List<ItemStack> loot = event.getLoot();

        if (loot.isEmpty()) {
            return;
        }
        final List<ItemStack> newLoot = new ArrayList<>();

        for (final ItemStack itemStack : loot) {
            this.handle(itemStack, newLoot::add);
        }
        event.setLoot(newLoot);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityDeath(final EntityDeathEvent event) {
        final List<ItemStack> drops = new ArrayList<>(event.getDrops());

        event.getDrops().clear();

        for (final ItemStack itemStack : drops) {
            this.handle(itemStack, (result) -> event.getDrops().add(result));
        }
    }

    private void handle(@NotNull final ItemStack itemStack, @NotNull final Consumer<ItemStack> setItemStackFunction) {
        final String vanillaId = "VANILLA_" + itemStack.getType().name();
        final CustomItem droppedCustomItem = this.customItemService.getCustomItem(itemStack)
                .orElse(null);

        if (droppedCustomItem != null && !droppedCustomItem.id().equals(vanillaId)) {
            return;
        }
        this.customItemService.getCustomItem(vanillaId).ifPresent((customItem) -> {
            setItemStackFunction.accept(this.customItemService.createItemStack(customItem));
        });
    }
}
