package ravioli.gravioli.core.animation.model;

public enum AnimationDirection {
    NORMAL,
    REVERSE,
    ALTERNATE,
    ALTERNATE_REVERSE;

    public boolean isAlternating() {
        return switch (this) {
            case ALTERNATE, ALTERNATE_REVERSE -> true;
            default -> false;
        };
    }

    public boolean isReverse() {
        return switch (this) {
            case REVERSE, ALTERNATE_REVERSE -> true;
            default -> false;
        };
    }
}
