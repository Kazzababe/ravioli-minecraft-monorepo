package ravioli.gravioli.core.animation.timing;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EaseInTimingFunction implements AnimationTiming {
    @Override
    public long timeUntilFrame(final int currentFrame, final int totalFrames,
                                     @NotNull final Duration animationDuration) {
        final long totalDuration = animationDuration.toMillis();
        final double progress = (double) (currentFrame + 1) / totalFrames;
        final double adjustedProgress = Math.pow(progress, 3);

        return (long) (totalDuration * adjustedProgress);
    }
}
