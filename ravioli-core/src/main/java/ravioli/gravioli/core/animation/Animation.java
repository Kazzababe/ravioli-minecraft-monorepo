package ravioli.gravioli.core.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.animation.listener.AnimationCompletionListener;
import ravioli.gravioli.core.animation.listener.AnimationIterationCompletionListener;
import ravioli.gravioli.core.animation.model.AnimationDirection;
import ravioli.gravioli.core.animation.model.AnimationRepeatBehavior;

import java.time.Duration;

public interface Animation {
    /**
     * Start the animation.
     */
    void start();

    /**
     * Stop the animation.
     */
    void stop();

    /**
     * This function is called after the appropriate amount of time has passed since the previous frame has been
     * stepped through.
     *
     * @param currentFrame      The index of the current frame
     */
    void step(int currentFrame);

    /**
     * Get the total amount of frames in the animation.
     *
     * @return      The total frames
     */
    int getTotalFrames();

    /**
     * Get the total {@link Duration} of the animation.
     *
     * @return      The duration of the animation
     */
    @NotNull Duration getDuration();

    /**
     * How many times the animation will play. Any value below {@code 1} will cause the animation to play infinitely.
     *
     * @return      The iteration count of the animation.
     */
    default int getIterationCount() {
        return 1;
    }

    /**
     * Get the direction the animation moves in.
     *
     * @return      The direction the animation moves in.
     */
    default @NotNull AnimationDirection getAnimationDirection() {
        return AnimationDirection.NORMAL;
    }

    /**
     * Get the delay before the animation runs for the first time.
     * 
     * @return      The initial animation delay
     */
    default @NotNull Duration getInitialAnimationDelay() {
        return Duration.ZERO;
    }

    /**
     * If the animation has multiple iterations this will determine how the animation is reset after an
     * iteration completes.
     *
     * @return      The repeat animation behavior
     */
    default @NotNull AnimationRepeatBehavior getAnimationRepeatBehavior() {
        return AnimationRepeatBehavior.REPEAT_FIRST_FRAME;
    }

    /**
     * Get the delay between every iteration of the animation.
     *
     * @return      The repeat animation delay
     */
    default Duration getRepeatAnimationDelay() {
        return Duration.ZERO;
    }

    /**
     * Set the listener that will fire off whenever the animation is finished.
     *
     * @param listener      The completion listener.
     */
    void setAnimationCompletionListener(@Nullable AnimationCompletionListener listener);

    /**
     * Set the listener that will fire off whenever the animation completes an iteration.
     *
     * @param listener      The iteration completion listener.
     */
    void setAnimationIterationCompletionListener(@Nullable AnimationIterationCompletionListener listener);

    /**
     * Return whether the animation is currently running.
     *
     * @return      True if the animation is running; false otherwise
     */
    boolean isRunning();
}
