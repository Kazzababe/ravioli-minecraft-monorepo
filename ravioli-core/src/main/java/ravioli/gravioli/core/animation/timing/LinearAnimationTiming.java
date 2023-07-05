package ravioli.gravioli.core.animation.timing;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class LinearAnimationTiming implements AnimationTiming {
    @Override
    public long timeUntilFrame(final int currentFrame, final int totalFrames,
                               @NotNull final Duration animationDuration) {
        final long totalDuration = animationDuration.toMillis();
        final long interval = totalDuration / totalFrames;

        return interval * currentFrame;
    }
}
