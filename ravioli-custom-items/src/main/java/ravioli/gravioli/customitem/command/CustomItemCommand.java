package ravioli.gravioli.customitem.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.command.common.context.CommandContext;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.command.RavioliCommand;
import ravioli.gravioli.customitem.service.CustomItemService;

import java.util.Objects;

public class CustomItemCommand extends RavioliCommand {
    private final CustomItemService customItemService;

    public CustomItemCommand() {
        super("ci", "customitem", "customitems");

        this.customItemService = Objects.requireNonNull(Platform.loadService(CustomItemService.class));
    }

    @Override
    protected void init() {
        this.registerSubCommand(
                DEFAULT_COMMAND_META,
                Literal("give"),
                String("item")
                        .exceptionally((source, e) -> {
                            e.printStackTrace();
                            source.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                        })
                        .executor(this::onGiveVanilla)
        );
    }

    private void onGiveVanilla(@NotNull final CommandContext<CommandSender> context) {
        if (!(context.getSource() instanceof final Player player)) {
            return;
        }
        final String itemId = context.get("item");

        this.customItemService.getCustomItem(itemId).ifPresentOrElse((customItem) -> {
            final ItemStack itemStack = this.customItemService.createItemStack(customItem);

            player.getInventory().addItem(itemStack);
            player.sendMessage("Gave yourself custom item: " + customItem.id());
        }, () -> player.sendMessage("no item with that id"));
    }
}
