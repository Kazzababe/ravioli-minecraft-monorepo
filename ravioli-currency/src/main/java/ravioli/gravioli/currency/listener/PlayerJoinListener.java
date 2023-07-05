package ravioli.gravioli.currency.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.core.resourcepack.overlay.OverlayComponent;
import ravioli.gravioli.core.resourcepack.overlay.actionbar.ActionBarOverlayManager;
import ravioli.gravioli.currency.event.UserBalanceChangeEvent;
import ravioli.gravioli.currency.overlay.CurrencyOverlayComponent;

public class PlayerJoinListener implements Listener {
    public static final OverlayComponent CURRENCY_COMPONENT = new CurrencyOverlayComponent();

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        this.updateCurrencyDisplay(player);
    }

    @EventHandler
    private void onUserBalanceUpdate(final UserBalanceChangeEvent event) {
        final User user = event.getUser();
        final Player player = Bukkit.getPlayer(user.uuid());

        if (player == null) {
            return;
        }
        this.updateCurrencyDisplay(player);
    }

    private void updateCurrencyDisplay(@NotNull final Player player) {
        ActionBarOverlayManager.get().showPlayer(player, "CURRENCY", CURRENCY_COMPONENT);
    }
}
