package ravioli.gravioli.core.resourcepack.layout.properties;

import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

public class ResourcePackComponentProperties {
    private static final Key DEFAULT_FONT = Key.key("minecraft", "default");

    private Alignment alignment;
    private Pair<Integer, Integer> position;
    private Pair<Integer, Integer> dimensions;
    private Key font;

    public ResourcePackComponentProperties() {
        this.position = new ImmutablePair<>(0, 0);
        this.dimensions = new ImmutablePair<>(0, 0);
        this.font = DEFAULT_FONT;
    }

    public void setAlignment(@NotNull final Alignment alignment) {
        this.alignment = alignment;
    }

    public @Nullable Alignment getAlignment() {
        return this.alignment;
    }

    public void setPosition(final int x, final int y) {
        this.position = new ImmutablePair<>(x, y);
    }

    public @NotNull Pair<Integer, Integer> getPosition() {
        return this.position;
    }

    public void setDimensions(final int width, final int height) {
        this.dimensions = new ImmutablePair<>(width, height);
    }

    public @NotNull Pair<Integer, Integer> getDimensions() {
        return this.dimensions;
    }

    public void setFont(@NotNull final Key font) {
        this.font = font;
    }

    public @NotNull Key getFont() {
        return this.font;
    }
}
