package ravioli.gravioli.core.animation;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.animation.listener.AnimationCompletionListener;
import ravioli.gravioli.core.animation.listener.AnimationIterationCompletionListener;
import ravioli.gravioli.core.animation.model.AnimationDirection;
import ravioli.gravioli.core.animation.timing.AnimationTiming;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public abstract class ExternalBukkitAnimation implements Animation {
    private final AnimationTiming animationTiming;

    private NavigableMap<Long, Integer> frameTiming;
    private NavigableMap<Long, Integer> originalFrameTiming;
    private long startTime;
    private int currentIterations;
    private AnimationCompletionListener animationCompletionListener;
    private AnimationIterationCompletionListener animationIterationCompletionListener;

    public ExternalBukkitAnimation(@NotNull final AnimationTiming animationTiming) {
        this.animationTiming = animationTiming;
    }

    @Override
    public synchronized void start() {
        this.startTime = Instant.now().toEpochMilli() + this.getInitialAnimationDelay().toMillis();
        this.currentIterations = 0;

        this.populateFrameData();
    }

    @Override
    public void setAnimationCompletionListener(@Nullable final AnimationCompletionListener listener) {
        this.animationCompletionListener = listener;
    }

    @Override
    public void setAnimationIterationCompletionListener(@Nullable final AnimationIterationCompletionListener listener) {
        this.animationIterationCompletionListener = listener;
    }

    public synchronized void run() {
        final long now = Instant.now().toEpochMilli();
        final long timePassed = now - this.startTime;

        if (timePassed < 0) {
            return;
        }
        final Map.Entry<Long, Integer> currentFrameEntry = this.frameTiming.floorEntry(timePassed);

        if (currentFrameEntry != null) {
            final int currentFrame = currentFrameEntry.getValue();

            this.frameTiming.remove(currentFrameEntry.getKey());
            this.step(currentFrame);
        }
        if (timePassed <= this.getDuration().toMillis()) {
            return;
        }
        final int totalIterations = this.getIterationCount();

        if (this.animationIterationCompletionListener != null) {
            this.animationIterationCompletionListener.onAnimationIterationComplete(this.currentIterations);
        }
        if (totalIterations > 0 && ++this.currentIterations >= totalIterations) {
            this.stop();

            if (this.animationCompletionListener != null) {
                this.animationCompletionListener.onAnimationComplete();
            }
            return;
        }
        this.frameTiming = new TreeMap<>(this.originalFrameTiming);

        final long timeOffset = switch (this.getAnimationRepeatBehavior()) {
            case REPEAT_FIRST_FRAME -> 0;
            case SKIP_FIRST_FRAME -> {
                this.frameTiming.remove(0L);

                yield this.frameTiming.ceilingKey(1L);
            }
        };

        this.startTime = now + this.getRepeatAnimationDelay().toMillis() - timeOffset;
    }

    private void populateFrameData() {
        final int totalFrames = this.getTotalFrames();

        if (totalFrames <= 0) {
            throw new IllegalStateException("Cannot create an animation with zero frames.");
        }
        final AnimationDirection direction = this.getAnimationDirection();
        final boolean alternating = direction.isAlternating();
        final boolean reverse = direction.isReverse();
        final Duration duration = this.getDuration().dividedBy(alternating ? 2 : 1);
        final Map<Integer, Long> delays = new HashMap<>();

        this.frameTiming = new TreeMap<>();

        for (int i = 0; i < totalFrames; i++) {
            final long delay = animationTiming.timeUntilFrame(i, totalFrames, duration);
            final int frame = reverse ? totalFrames - 1 - i : i;

            this.frameTiming.put(delay, frame);
            delays.put(frame, delay);
        }
        if (alternating) {
            long currentTime = delays.get(totalFrames - 1);

            for (int i = totalFrames - 1; i >= 0; i--) {
                final long nextDelay = delays.get(i);
                final long forwardDelay = delays.getOrDefault(i + 1, nextDelay);
                final long delay = forwardDelay - nextDelay;
                final int frame = reverse ? totalFrames - 1 - i : i;

                currentTime = currentTime + delay;
                this.frameTiming.put(currentTime, frame);
            }
        }
        this.originalFrameTiming = Maps.unmodifiableNavigableMap(new TreeMap<>(this.frameTiming));
    }
}
