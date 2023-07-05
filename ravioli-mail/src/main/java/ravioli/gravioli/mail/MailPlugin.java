package ravioli.gravioli.mail;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import ravioli.gravioli.command.bukkit.BukkitCommandManager;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.api.RavioliPlugin;
import ravioli.gravioli.mail.command.MailCommand;
import ravioli.gravioli.mail.service.MailService;
import ravioli.gravioli.mail.service.RestfulMailService;

public class MailPlugin extends RavioliPlugin {
    private final MailService mailService;
    private final BukkitCommandManager commandManager;

    public MailPlugin() {
        this.mailService = new RestfulMailService();
        this.commandManager = new BukkitCommandManager(this);

        Platform.registerService(MailService.class, this.mailService);
    }

    @Override
    protected void onPluginEnable() {
        this.commandManager.register(new MailCommand(this));
    }
}
