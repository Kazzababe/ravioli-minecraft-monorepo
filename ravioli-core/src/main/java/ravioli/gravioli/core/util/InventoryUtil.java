package ravioli.gravioli.core.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtil {
    /**
     * Returns whether an {@link ItemStack} will fit into an {@link Inventory}.
     *
     * @param inventory         the inventory check
     * @param itemStack         the item to check
     * @return                  {@code true} if the item would fit; {@code false} otherwise
     */
    public static boolean fits(@NotNull final Inventory inventory, @NotNull final ItemStack itemStack) {
        final Inventory clone = Bukkit.createInventory(
                null,
                inventory instanceof PlayerInventory ?
                        9 * 4 :
                        inventory.getSize()
        );

        for (int i = 0; i < clone.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null) {
                continue;
            }
            clone.setItem(i, item.clone());
        }
        return clone.addItem(itemStack).isEmpty();
    }
}
