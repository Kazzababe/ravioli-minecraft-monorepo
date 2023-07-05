package ravioli.gravioli.customitem.item;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.util.ItemBuilder;
import ravioli.gravioli.customitem.config.item.VanillaItemDetails;
import ravioli.gravioli.customitem.item.data.VanillaCustomItemData;

public class VanillaCustomItem extends AbstractCustomItem<VanillaCustomItemData> {
    private final Material material;
    private final VanillaItemDetails details;

    public VanillaCustomItem(@NotNull final Material material, @NotNull final VanillaItemDetails details) {
        super("VANILLA_" + material.name());

        this.material = material;
        this.details = details;
    }

    @Override
    public @NotNull ItemBuilder itemBuilder(@NotNull final VanillaCustomItemData itemData) {
        final ItemBuilder itemBuilder = new ItemBuilder(this.material)
                .addItemFlags(itemData.itemFlags().toArray(ItemFlag[]::new));
        final Component itemDataDisplayName = itemData.displayName();

        if (itemDataDisplayName != null) {
            itemBuilder.displayName(itemDataDisplayName);
        } else if (this.details.displayName() != null) {
            itemBuilder.displayName(MiniMessage.miniMessage().deserialize(this.details.displayName()));
        }
        itemData.enchantments().forEach(itemBuilder::addEnchantment);

        if (this.details.lore().size() > 0) {
            itemBuilder.addLore(
                    this.details.lore()
                            .stream()
                            .map((line) -> MiniMessage.miniMessage().deserialize(line))
                            .toArray(Component[]::new)
            );
        }
        return itemBuilder;
    }

    @Override
    public @NotNull VanillaCustomItemData extractItemData(@NotNull final ItemStack itemStack) {
        return new VanillaCustomItemData(itemStack);
    }

    @Override
    public @NotNull VanillaCustomItemData createDefaultItemData() {
        return new VanillaCustomItemData();
    }

    @Override
    public @NotNull VanillaCustomItemData deserializeData(@NotNull final JsonObject data) {
        return new VanillaCustomItemData(data);
    }
}
