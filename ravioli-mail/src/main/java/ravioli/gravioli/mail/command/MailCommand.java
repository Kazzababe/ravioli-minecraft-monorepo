package ravioli.gravioli.mail.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.command.common.context.CommandContext;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.command.RavioliCommand;
import ravioli.gravioli.mail.MailPlugin;
import ravioli.gravioli.mail.menu.ServerMailMenu;
import ravioli.gravioli.mail.service.MailService;

import java.util.Objects;

public class MailCommand extends RavioliCommand {
    private final MailPlugin plugin;
    private final MailService mailService;

    public MailCommand(@NotNull final MailPlugin plugin) {
        super("mail");

        this.plugin = plugin;
        this.mailService = Objects.requireNonNull(Platform.loadService(MailService.class));
    }

    @Override
    protected void init() {
        this.asyncExecutor(this::onMailMenu);
        this.exceptionally((source, e) -> {
            source.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
        });
    }

    private void onMailMenu(@NotNull final CommandContext<CommandSender> context) {
        if (!(context.getSource() instanceof final Player player)) {
            return;
        }
        new ServerMailMenu(this.plugin, player).open();
    }
}
