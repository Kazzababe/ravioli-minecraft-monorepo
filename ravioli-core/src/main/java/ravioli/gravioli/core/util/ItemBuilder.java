package ravioli.gravioli.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public final class ItemBuilder {
    private static final Style FALLBACK_STYLE = Style.style()
            .decoration(TextDecoration.ITALIC, false)
            .build();
    private static final PersistentDataType<?, ?>[] PERSISTENT_DATA_TYPES = new PersistentDataType[]{
            PersistentDataType.BYTE,
            PersistentDataType.SHORT,
            PersistentDataType.INTEGER,
            PersistentDataType.LONG,
            PersistentDataType.FLOAT,
            PersistentDataType.DOUBLE,
            PersistentDataType.STRING,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY,
            PersistentDataType.TAG_CONTAINER,
            PersistentDataType.TAG_CONTAINER_ARRAY
    };

    private final Material material;

    private int amount;
    private Component displayName;
    private List<Component> lore;
    private Set<ItemFlag> itemFlags;
    private Integer customModelData;
    private Boolean unbreakable;
    private Map<Enchantment, Integer> enchantments;
    private Map<Class<? extends ItemMeta>, Consumer<ItemMeta>> itemMetaModifiers;

    public ItemBuilder(@NotNull final Material material) {
        this(material, 1);
    }

    public ItemBuilder(@NotNull final Material material, final int amount) {
        this.material = material;
        this.amount = amount;
        this.lore = new ArrayList<>();
        this.itemFlags = new HashSet<>();
        this.itemMetaModifiers = new HashMap<>();
    }

    public ItemBuilder(@NotNull final ItemStack itemStack) {
        this(itemStack.getType(), itemStack.getAmount());

        final ItemMeta currentItemMeta = itemStack.getItemMeta();

        if (currentItemMeta == null) {
            return;
        }
        this.displayName = currentItemMeta.displayName();
        this.lore = new ArrayList<>(Objects.requireNonNullElse(currentItemMeta.lore(), new ArrayList<>()));
        this.itemFlags = new HashSet<>(currentItemMeta.getItemFlags());
        this.customModelData = currentItemMeta.hasCustomModelData() ? currentItemMeta.getCustomModelData() : null;
        this.unbreakable = currentItemMeta.isUnbreakable();
        this.enchantments = new HashMap<>(currentItemMeta.getEnchants());

        if (currentItemMeta instanceof final LeatherArmorMeta leatherArmorMeta) {
            this.modifier(LeatherArmorMeta.class, (itemMeta) -> itemMeta.setColor(leatherArmorMeta.getColor()));
        } else if (currentItemMeta instanceof final SkullMeta skullMeta) {
            this.modifier(SkullMeta.class, (itemMeta) -> {
                itemMeta.setPlayerProfile(skullMeta.getPlayerProfile());
                itemMeta.setOwningPlayer(skullMeta.getOwningPlayer());
            });
        } else if (currentItemMeta instanceof final FireworkMeta fireworkMeta) {
            this.modifier(FireworkMeta.class, (itemMeta) -> {
                fireworkMeta.getEffects().forEach(itemMeta::addEffect);

                itemMeta.setPower(fireworkMeta.getPower());
            });
        } else if (currentItemMeta instanceof final BannerMeta bannerMeta) {
            this.modifier(BannerMeta.class, (itemMeta) -> {
                bannerMeta.getPatterns().forEach(itemMeta::addPattern);
            });
        } else if (currentItemMeta instanceof final EnchantmentStorageMeta enchantmentStorageMeta) {
            this.modifier(EnchantmentStorageMeta.class, (itemMeta) -> {
                enchantmentStorageMeta.getStoredEnchants().forEach((enchantment, level) ->
                        itemMeta.addStoredEnchant(enchantment, level, true)
                );
            });
        }
        final PersistentDataContainer currentContainer = currentItemMeta.getPersistentDataContainer();

        currentContainer.getKeys().forEach((key) -> {
            this.itemMetaModifiers.put(ItemMeta.class, (itemMeta) -> {
                final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

                for (final PersistentDataType type : PERSISTENT_DATA_TYPES) {
                    try {
                        final Object value = currentContainer.get(key, type);

                        container.set(key, type, value);
                    } catch (final Exception ignored) {
                        continue;
                    }
                    break;
                }
            });
        });
    }

    public ItemBuilder(@NotNull final ItemBuilder itemBuilder) {
        this(itemBuilder.material, itemBuilder.amount);

        this.displayName = itemBuilder.displayName;
        this.lore = new ArrayList<>(itemBuilder.lore);
        this.itemFlags = new HashSet<>(itemBuilder.itemFlags);
        this.itemMetaModifiers = new HashMap<>(itemBuilder.itemMetaModifiers);
        this.customModelData = itemBuilder.customModelData;
        this.unbreakable = itemBuilder.unbreakable;
        this.enchantments = new HashMap<>(itemBuilder.enchantments);
    }

    public @NotNull ItemBuilder amount(final int amount) {
        this.amount = amount;

        return this;
    }

    public @NotNull ItemBuilder displayName(@NotNull final Component displayName) {
        this.displayName = displayName;

        return this;
    }

    public @NotNull ItemBuilder displayName(@NotNull final String displayName, final boolean parseMiniMessage) {
        this.displayName =
                parseMiniMessage ? MiniMessage.miniMessage().deserialize(displayName) : Component.text(displayName);

        return this;
    }

    public @NotNull ItemBuilder displayName(@NotNull final String displayName) {
        return this.displayName(displayName, false);
    }

    public @NotNull ItemBuilder lore(@NotNull final List<Component> lore) {
        this.lore = lore;

        return this;
    }

    public @NotNull ItemBuilder lore(@NotNull final Component... lore) {
        this.lore = new ArrayList<>(List.of(lore));

        return this;
    }

    public @NotNull ItemBuilder addLore(@NotNull final Component... lore) {
        this.lore.addAll(List.of(lore));

        return this;
    }

    public @NotNull ItemBuilder customModelData(final int customModelData) {
        this.customModelData = customModelData;

        return this;
    }

    public @NotNull ItemBuilder unbreakable(final boolean unbreakable) {
        this.unbreakable = unbreakable;

        return this;
    }

    public @NotNull ItemBuilder addEnchantment(@NotNull final Enchantment enchantment, final int level) {
        if (enchantments == null) {
            this.enchantments = new HashMap<>();
        }
        this.enchantments.put(enchantment, level);

        return this;
    }

    public @NotNull ItemBuilder addItemFlags(@NotNull final ItemFlag... itemFlags) {
        this.itemFlags.addAll(List.of(itemFlags));

        return this;
    }

    @SuppressWarnings({"unchecked", "rawTypes"})
    public @NotNull <T extends ItemMeta> ItemBuilder modifier(@NotNull final Class<T> itemMetaClass,
                                                              @NotNull final Consumer<T> itemMetaConsumer) {
        this.itemMetaModifiers.merge(itemMetaClass, (Consumer<ItemMeta>) itemMetaConsumer, (a, b) -> (itemMeta) -> {
            a.accept(itemMeta);
            b.accept(itemMeta);
        });

        return this;
    }

    public @NotNull ItemStack build() {
        final ItemStack itemStack = new ItemStack(this.material, this.amount);

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        if (this.displayName != null) {
            itemMeta.displayName(this.displayName.applyFallbackStyle(FALLBACK_STYLE));
        }
        if (this.lore.size() > 0) {
            itemMeta.lore(this.lore.stream()
                    .map(line -> line.applyFallbackStyle(FALLBACK_STYLE))
                    .toList());
        }
        if (this.customModelData != null) {
            itemMeta.setCustomModelData(this.customModelData);
        }
        if (this.unbreakable != null) {
            itemMeta.setUnbreakable(this.unbreakable);
        }
        if (this.enchantments != null) {
            this.enchantments.forEach((enchantment, level) -> {
                itemMeta.addEnchant(enchantment, level, true);
            });
        }
        if (this.itemMetaModifiers.size() > 0) {
            this.itemMetaModifiers.forEach((itemMetaClass, consumer) -> {
                if (!itemMetaClass.isAssignableFrom(itemMeta.getClass())) {
                    return;
                }
                consumer.accept(itemMeta);
            });
        }
        itemMeta.addItemFlags(this.itemFlags.toArray(ItemFlag[]::new));
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
