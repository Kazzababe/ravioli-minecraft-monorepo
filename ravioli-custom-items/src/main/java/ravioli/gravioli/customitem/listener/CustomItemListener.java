package ravioli.gravioli.customitem.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.customitem.service.CustomItemService;
import ravioli.gravioli.customitem.action.CustomItemAction;
import ravioli.gravioli.customitem.action.event.CustomItemClickAirEvent;
import ravioli.gravioli.customitem.action.event.CustomItemClickBlockEvent;
import ravioli.gravioli.customitem.action.event.CustomItemEvent;
import ravioli.gravioli.customitem.behavior.CustomItemFlag;
import ravioli.gravioli.customitem.item.CustomItem;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CustomItemListener implements Listener {
    private final CustomItemService customItemService;

    public CustomItemListener() {
        this.customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onClickBlock(final PlayerInteractEvent event) {
        final Action action = event.getAction();

        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final ItemStack itemStack = event.getItem();

        this.handleEvent(itemStack, CustomItemAction.CLICK_BLOCK, () -> new CustomItemClickBlockEvent(event));
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onClickAir(final PlayerInteractEvent event) {
        final Action action = event.getAction();

        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            return;
        }
        final ItemStack itemStack = event.getItem();

        this.handleEvent(itemStack, CustomItemAction.CLICK_AIR, () -> new CustomItemClickAirEvent(event));
    }

    @EventHandler(ignoreCancelled = true)
    private void onRename(final InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof final AnvilInventory anvilInventory)) {
            return;
        }
        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        final ItemStack result = anvilInventory.getResult();
        final ItemStack comparison = anvilInventory.getFirstItem();

        if (result == null || result.getType().isAir()) {
            return;
        }
        if (comparison == null || comparison.getType().isAir()) {
            return;
        }
        final ItemMeta resultMeta = result.getItemMeta();
        final ItemMeta comparisonMeta = comparison.getItemMeta();

        if (!resultMeta.hasDisplayName() && !comparisonMeta.hasDisplayName()) {
            return;
        }
        if (Objects.equals(resultMeta.displayName(), comparisonMeta.displayName())) {
            return;
        }
        final CustomItem<?> customItem = this.customItemService.getCustomItem(result)
                .orElse(null);

        if (customItem == null) {
            return;
        }
        if (customItem.hasFlag(CustomItemFlag.CANNOT_BE_RENAMED)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("This item cannot be renamed.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends CustomItemEvent> void handleEvent(@Nullable final ItemStack itemStack,
                                                         @NotNull final CustomItemAction<T> action,
                                                         @NotNull final Supplier<T> eventSupplier) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }
        final CustomItem<?> customItem = this.customItemService.getCustomItem(itemStack)
                .orElse(null);

        if (customItem == null) {
            return;
        }
        final Consumer<T> itemEvent = customItem.actionHandler(action)
                .orElse(null);

        if (itemEvent == null) {
            return;
        }
        itemEvent.accept(eventSupplier.get());
    }
}
