package ravioli.gravioli.auctionhouse.model;

import org.jetbrains.annotations.NotNull;

public enum SortDirection {
    ASCENDING ("Ascending"),
    DESCENDING ("Descending");

    private final String displayName;

    SortDirection(@NotNull final String displayName) {
        this.displayName = displayName;
    }

    public @NotNull String displayName() {
        return this.displayName;
    }
}
