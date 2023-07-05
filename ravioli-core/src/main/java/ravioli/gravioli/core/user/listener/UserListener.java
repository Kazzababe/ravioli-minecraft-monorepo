package ravioli.gravioli.core.user.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.common.user.data.User;
import ravioli.gravioli.common.user.service.UserService;

import java.util.Objects;
import java.util.UUID;

public class UserListener implements Listener {
    @EventHandler
    private void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));
        final UUID uuid = event.getUniqueId();
        final String username = event.getName();
        User user = userService.loadByUuid(uuid).join();

        if (user == null) {
            user = userService.createUser(uuid, username)
                .join();
        } else if (!user.username().equals(username)) {
            user = userService.updateUsername(user, username)
                .join();
        }
        userService.cache(user);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final UserService userService = Objects.requireNonNull(Platform.loadService(UserService.class));

        userService.getByUuid(event.getPlayer().getUniqueId()).ifPresent(userService::invalidate);
    }
}
