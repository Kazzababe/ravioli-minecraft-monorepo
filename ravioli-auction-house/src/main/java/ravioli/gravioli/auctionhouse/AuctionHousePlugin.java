package ravioli.gravioli.auctionhouse;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import ravioli.gravioli.auctionhouse.command.AuctionHouseCommand;
import ravioli.gravioli.auctionhouse.config.AuctionHouseConfiguration;
import ravioli.gravioli.auctionhouse.service.AuctionHouseService;
import ravioli.gravioli.auctionhouse.service.RestfulAuctionHouseService;
import ravioli.gravioli.command.bukkit.BukkitCommandManager;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.api.RavioliPlugin;

public class AuctionHousePlugin extends RavioliPlugin {
    private final AuctionHouseService auctionHouseService;
    private final AuctionHouseConfiguration auctionHouseConfiguration;
    private final BukkitCommandManager commandManager;

    public AuctionHousePlugin() {
        this.saveDefaultConfig();

        this.auctionHouseService = new RestfulAuctionHouseService();
        this.auctionHouseConfiguration = new AuctionHouseConfiguration();
        this.commandManager = new BukkitCommandManager(this);

        Platform.registerService(AuctionHouseService.class, this.auctionHouseService);
    }

    @Override
    protected void onPluginLoad() {
        this.auctionHouseConfiguration.load(this.getConfig());
    }

    @Override
    protected void onPluginEnable() {
        this.commandManager.register(new AuctionHouseCommand(this, this.getLocale(), this.auctionHouseService, this.auctionHouseConfiguration));
    }
}
