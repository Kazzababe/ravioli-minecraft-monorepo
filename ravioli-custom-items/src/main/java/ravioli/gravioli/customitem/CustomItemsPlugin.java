package ravioli.gravioli.customitem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import ravioli.gravioli.command.bukkit.BukkitCommandManager;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.api.RavioliPlugin;
import ravioli.gravioli.customitem.command.CustomItemCommand;
import ravioli.gravioli.customitem.config.CustomItemConfiguration;
import ravioli.gravioli.customitem.config.item.VanillaItemDetails;
import ravioli.gravioli.customitem.enchantment.FakeGlowEnchantment;
import ravioli.gravioli.customitem.item.VanillaCustomItem;
import ravioli.gravioli.customitem.listener.CustomItemListener;
import ravioli.gravioli.customitem.service.CustomItemService;
import ravioli.gravioli.customitem.service.RavioliCustomItemService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;

public class CustomItemsPlugin extends RavioliPlugin {
    private final CustomItemConfiguration customItemConfiguration;
    private final RavioliCustomItemService customItemService;
    private final BukkitCommandManager commandManager;

    public CustomItemsPlugin() {
        this.saveDefaultConfig();

        this.customItemConfiguration = new CustomItemConfiguration();
        this.customItemService = new RavioliCustomItemService();
        this.commandManager = new BukkitCommandManager(this);

        this.customItemService.setCallCreationEvent(false);
        Platform.registerService(CustomItemService.class, this.customItemService);
    }

    @Override
    protected void onPluginLoad() {
        this.registerFakeGlowEnchantment();
        this.customItemConfiguration.load(this.getConfig());

        for (final Material material : Material.values()) {
            final VanillaItemDetails details = this.customItemConfiguration.getDetails(material)
                .orElse(new VanillaItemDetails(null, Collections.emptyList()));

            this.customItemService.registerCustomItem(new VanillaCustomItem(material, details));
        }
    }

    @Override
    protected void onPluginEnable() {
        this.commandManager.register(new CustomItemCommand());
        this.replaceVanillaCraftingRecipes();

        Bukkit.getPluginManager().registerEvents(new CustomItemListener(), this);
    }

    private void registerFakeGlowEnchantment() {
        try {
            final Field field = Enchantment.class.getDeclaredField("acceptingNew");

            field.setAccessible(true);
            field.set(null, true);

            Enchantment.registerEnchantment(FakeGlowEnchantment.GLOW_ENCHANTMENT);

            field.set(null, false);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void replaceVanillaCraftingRecipes() {
        final Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();

        Bukkit.clearRecipes();

        while (recipeIterator.hasNext()) {
            final Recipe recipe = recipeIterator.next();
            final ItemStack result = recipe.getResult();
            final String vanillaId = "VANILLA_" + result.getType().name();

            this.customItemService.getCustomItem(vanillaId).ifPresentOrElse((customItem) -> {
                final ItemStack newResult = this.customItemService.createItemStack(customItem);
                final Recipe resultingRecipe;

                if (recipe instanceof final ShapelessRecipe shapelessRecipe) {
                    final ShapelessRecipe newRecipe = new ShapelessRecipe(shapelessRecipe.getKey(), newResult);

                    newRecipe.setCategory(shapelessRecipe.getCategory());
                    newRecipe.setGroup(shapelessRecipe.getGroup());

                    shapelessRecipe.getChoiceList().forEach(newRecipe::addIngredient);
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final ShapedRecipe shapedRecipe) {
                    final ShapedRecipe newRecipe = new ShapedRecipe(shapedRecipe.getKey(), newResult);

                    newRecipe.setCategory(shapedRecipe.getCategory());
                    newRecipe.setGroup(shapedRecipe.getGroup());
                    newRecipe.shape(shapedRecipe.getShape());

                    shapedRecipe.getChoiceMap().forEach(newRecipe::setIngredient);
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final BlastingRecipe blastingRecipe) {
                    final BlastingRecipe newRecipe = new BlastingRecipe(
                        blastingRecipe.getKey(),
                        newResult,
                        blastingRecipe.getInputChoice(),
                        blastingRecipe.getExperience(),
                        blastingRecipe.getCookingTime()
                    );

                    newRecipe.setCategory(blastingRecipe.getCategory());
                    newRecipe.setGroup(blastingRecipe.getGroup());
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final CampfireRecipe campfireRecipe) {
                    final CampfireRecipe newRecipe = new CampfireRecipe(
                        campfireRecipe.getKey(),
                        newResult,
                        campfireRecipe.getInputChoice(),
                        campfireRecipe.getExperience(),
                        campfireRecipe.getCookingTime()
                    );

                    newRecipe.setCategory(campfireRecipe.getCategory());
                    newRecipe.setGroup(campfireRecipe.getGroup());
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final FurnaceRecipe furnaceRecipe) {
                    final FurnaceRecipe newRecipe = new FurnaceRecipe(
                        furnaceRecipe.getKey(),
                        newResult,
                        furnaceRecipe.getInputChoice(),
                        furnaceRecipe.getExperience(),
                        furnaceRecipe.getCookingTime()
                    );

                    newRecipe.setCategory(furnaceRecipe.getCategory());
                    newRecipe.setGroup(furnaceRecipe.getGroup());
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final MerchantRecipe merchantRecipe) {
                    final MerchantRecipe newRecipe = new MerchantRecipe(
                        newResult,
                        merchantRecipe.getUses(),
                        merchantRecipe.getMaxUses(),
                        merchantRecipe.hasExperienceReward(),
                        merchantRecipe.getVillagerExperience(),
                        merchantRecipe.getPriceMultiplier(),
                        merchantRecipe.getDemand(),
                        merchantRecipe.getSpecialPrice(),
                        merchantRecipe.shouldIgnoreDiscounts()
                    );

                    merchantRecipe.setIngredients(merchantRecipe.getIngredients());
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final SmithingTransformRecipe smithingTransformRecipe) {
                    resultingRecipe = new SmithingTransformRecipe(
                        smithingTransformRecipe.getKey(),
                        newResult,
                        smithingTransformRecipe.getTemplate(),
                        smithingTransformRecipe.getBase(),
                        smithingTransformRecipe.getAddition()
                    );
                } else if (recipe instanceof final SmithingTrimRecipe smithingTrimRecipe) {
                    resultingRecipe = new SmithingTrimRecipe(
                        smithingTrimRecipe.getKey(),
                        smithingTrimRecipe.getTemplate(),
                        smithingTrimRecipe.getBase(),
                        smithingTrimRecipe.getAddition()
                    );
                } else if (recipe instanceof final SmithingRecipe smithingRecipe) {
                    resultingRecipe = new SmithingRecipe(
                        smithingRecipe.getKey(),
                        newResult,
                        smithingRecipe.getBase(),
                        smithingRecipe.getAddition(),
                        smithingRecipe.willCopyNbt()
                    );
                } else if (recipe instanceof final SmokingRecipe smokingRecipe) {
                    final SmokingRecipe newRecipe = new SmokingRecipe(
                        smokingRecipe.getKey(),
                        newResult,
                        smokingRecipe.getInputChoice(),
                        smokingRecipe.getExperience(),
                        smokingRecipe.getCookingTime()
                    );

                    newRecipe.setCategory(smokingRecipe.getCategory());
                    newRecipe.setGroup(smokingRecipe.getGroup());
                    resultingRecipe = newRecipe;
                } else if (recipe instanceof final StonecuttingRecipe stonecuttingRecipe) {
                    final StonecuttingRecipe newRecipe = new StonecuttingRecipe(
                        stonecuttingRecipe.getKey(),
                        newResult,
                        stonecuttingRecipe.getInputChoice()
                    );

                    newRecipe.setGroup(stonecuttingRecipe.getGroup());
                    resultingRecipe = newRecipe;
                } else {
                    resultingRecipe = recipe;
                }
                Bukkit.addRecipe(resultingRecipe);
            }, () -> Bukkit.addRecipe(recipe));
        }
        this.customItemService.setCallCreationEvent(true);
    }
}
