package ravioli.gravioli.core.animation.timing;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EaseOutTimingFunction implements AnimationTiming {
    @Override
    public long timeUntilFrame(final int currentFrame, final int totalFrames,
                               @NotNull final Duration animationDuration) {
        final long totalDuration = animationDuration.toMillis();
        final double progress = (double) currentFrame / totalFrames;
        final double adjustedProgress = 1 - Math.pow(1 - progress, 3);

        return (long) (totalDuration * adjustedProgress);
    }
}
