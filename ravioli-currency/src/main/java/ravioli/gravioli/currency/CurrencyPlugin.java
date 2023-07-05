package ravioli.gravioli.currency;

import org.bukkit.Bukkit;
import ravioli.gravioli.command.bukkit.BukkitCommandManager;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.currency.service.CurrencyConfigurationService;
import ravioli.gravioli.core.api.RavioliPlugin;
import ravioli.gravioli.currency.channel.UserBalanceUpdateMessageChannel;
import ravioli.gravioli.currency.command.CurrencyCommand;
import ravioli.gravioli.currency.config.CurrencyConfiguration;
import ravioli.gravioli.currency.listener.PlayerJoinListener;

public class CurrencyPlugin extends RavioliPlugin {
    private final CurrencyConfiguration currencyConfiguration;
    private final BukkitCommandManager commandManager;

    public CurrencyPlugin() {
        this.saveDefaultConfig();

        this.currencyConfiguration = new CurrencyConfiguration();
        this.commandManager = new BukkitCommandManager(this);

        Platform.registerService(CurrencyConfigurationService.class, this.currencyConfiguration);
    }

    @Override
    protected void onPluginLoad() {
        this.currencyConfiguration.reload(this.getConfig());

        this.redisProvider().registerChannel(new UserBalanceUpdateMessageChannel());
    }

    @Override
    protected void onPluginEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);

        this.commandManager.register(new CurrencyCommand());
    }
}
