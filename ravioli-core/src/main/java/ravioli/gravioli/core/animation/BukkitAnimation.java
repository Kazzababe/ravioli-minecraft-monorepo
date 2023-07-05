package ravioli.gravioli.core.animation;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.animation.timing.AnimationTiming;
import ravioli.gravioli.core.util.SchedulerUtil;

public abstract class BukkitAnimation extends ExternalBukkitAnimation implements Runnable {
    private BukkitTask task;
    private boolean running;

    public BukkitAnimation(@NotNull final AnimationTiming animationTiming) {
        super(animationTiming);
    }

    @Override
    public synchronized void stop() {
        if (this.task == null) {
            return;
        }
        this.task.cancel();

        this.task = null;
        this.running = false;
    }

    @Override
    public synchronized void start() {
        super.start();

        this.task = SchedulerUtil.async().runInterval(this, 1, 1);
        this.running = true;
    }

    @Override
    public synchronized final boolean isRunning() {
        return this.running;
    }
}
