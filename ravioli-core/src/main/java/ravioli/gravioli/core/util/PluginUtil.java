package ravioli.gravioli.core.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class PluginUtil {
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(PluginUtil.class);
    private static final Set<Consumer<Plugin>> PENDING_TASKS = new HashSet<>();
    private static boolean WAITING_FOR_ENABLE = false;

    public static void executeWhenEnabled(@NotNull final Consumer<Plugin> task) {
        if (PLUGIN.isEnabled()) {
            task.accept(PLUGIN);

            return;
        }
        synchronized (PENDING_TASKS) {
            PENDING_TASKS.add(task);

            if (WAITING_FOR_ENABLE) {
                return;
            }
            startWaitingThread();
        }
    }

    public static @NotNull <T extends Event> T callEvent(@NotNull final T event) {
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    private static void startWaitingThread() {
        WAITING_FOR_ENABLE = true;

        final Thread thread = new Thread(() -> {
            while (!PLUGIN.isEnabled()) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (PENDING_TASKS) {
                for (final Consumer<Plugin> task : PENDING_TASKS) {
                    task.accept(PLUGIN);
                }
                PENDING_TASKS.clear();
            }
        });

        thread.start();
    }
}
