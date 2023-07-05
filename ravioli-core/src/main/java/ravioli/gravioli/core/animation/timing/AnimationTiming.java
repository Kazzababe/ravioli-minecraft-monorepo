package ravioli.gravioli.core.animation.timing;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface AnimationTiming {
    AnimationTiming EASE_IN = new EaseInTimingFunction();
    AnimationTiming EASE_OUT = new EaseOutTimingFunction();
    AnimationTiming LINEAR = new LinearAnimationTiming();

    long timeUntilFrame(int currentFrame, int totalFrames, @NotNull Duration animationDuration);
}
