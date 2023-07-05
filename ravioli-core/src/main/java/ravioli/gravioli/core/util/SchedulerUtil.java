package ravioli.gravioli.core.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public final class SchedulerUtil {
    private static final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(SchedulerUtil.class);
    private static final Scheduler SYNC_SCHEDULER = new SyncScheduler();
    private static final Scheduler ASYNC_SCHEDULER = new AsyncScheduler();

    /**
     * Get an instance of a {@link Scheduler} that will execute tasks using Bukkit's synchronous scheduler.
     *
     * @return      A synchronous Scheduler instance.
     */
    public static Scheduler sync() {
        return SYNC_SCHEDULER;
    }

    /**
     * Get an instance of a {@link Scheduler} that will execute tasks using Bukkit's asynchronous scheduler.
     *
     * @return      An asynchronous Scheduler instance.
     */
    public static Scheduler async() {
        return ASYNC_SCHEDULER;
    }

    public interface Scheduler {
        @NotNull BukkitTask run(@NotNull Runnable runnable);

        @NotNull BukkitTask runLater(@NotNull Runnable runnable, long delay);

        @NotNull BukkitTask runInterval(@NotNull Runnable runnable, long initialDelay, long intervalDelay);

        void execute(@NotNull Runnable runnable);

        @NotNull Executor executor();
    }

    private static class SyncScheduler implements Scheduler {
        @Override
        public @NotNull BukkitTask run(@NotNull final Runnable runnable) {
            return Bukkit.getScheduler().runTask(PLUGIN, runnable);
        }

        @Override
        public @NotNull BukkitTask runLater(@NotNull final Runnable runnable, final long delay) {
            return Bukkit.getScheduler().runTaskLater(PLUGIN, runnable, delay);
        }

        @Override
        public @NotNull BukkitTask runInterval(@NotNull final Runnable runnable, final long initialDelay,
                                               final long intervalDelay) {
            return Bukkit.getScheduler().runTaskTimer(PLUGIN, runnable, initialDelay, intervalDelay);
        }

        @Override
        public void execute(@NotNull final Runnable runnable) {
            Bukkit.getScheduler().getMainThreadExecutor(PLUGIN).execute(runnable);
        }

        @Override
        public @NotNull Executor executor() {
            return Bukkit.getScheduler().getMainThreadExecutor(PLUGIN);
        }
    }

    private static class AsyncScheduler implements Scheduler {
        private static final Executor EXECUTOR = (task) -> {
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, task);
            } else {
                task.run();
            }
        };

        @Override
        public @NotNull BukkitTask run(@NotNull final Runnable runnable) {
            return Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, runnable);
        }

        @Override
        public @NotNull BukkitTask runLater(@NotNull final Runnable runnable, final long delay) {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(PLUGIN, runnable, delay);
        }

        @Override
        public @NotNull BukkitTask runInterval(@NotNull final Runnable runnable, final long initialDelay,
                                               final long intervalDelay) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(PLUGIN, runnable, initialDelay, intervalDelay);
        }

        @Override
        public void execute(@NotNull final Runnable runnable) {
            EXECUTOR.execute(runnable);
        }

        @Override
        public @NotNull Executor executor() {
            return EXECUTOR;
        }
    }
}
