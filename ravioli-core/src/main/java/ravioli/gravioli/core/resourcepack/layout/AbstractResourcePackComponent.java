package ravioli.gravioli.core.resourcepack.layout;

import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

public abstract class AbstractResourcePackComponent implements ResourcePackComponent {
    private final ResourcePackComponentProperties properties;

    private ResourcePackContainerComponent parent;

    public AbstractResourcePackComponent(@NotNull final ResourcePackComponentProperties properties) {
        this.properties = new PropogateableResourcePackComponentProperties(properties);
    }

    @Override
    public @NotNull ResourcePackComponentProperties properties() {
        return this.properties;
    }

    @Override
    public @Nullable ResourcePackContainerComponent parent() {
        return this.parent;
    }

    public void parent(@NotNull final ResourcePackContainerComponent parent) {
        this.parent = parent;
    }

    private final class PropogateableResourcePackComponentProperties extends ResourcePackComponentProperties {
        private final ResourcePackComponentProperties properties;

        private PropogateableResourcePackComponentProperties(@NotNull final ResourcePackComponentProperties properties) {
            this.properties = properties;
        }

        @Override
        public @Nullable Alignment getAlignment() {
            final Alignment currentAlignment = this.properties.getAlignment();

            if (currentAlignment != null) {
                return currentAlignment;
            }
            ResourcePackContainerComponent currentParent = parent;

            while (currentParent != null) {
                final Alignment alignment = currentParent.properties().getAlignment();

                if (alignment != null) {
                    return alignment;
                }
                currentParent = currentParent.parent();
            }
            return null;
        }

        @Override
        public @NotNull Pair<Integer, Integer> getPosition() {
            return this.properties.getPosition();
        }

        @Override
        public @NotNull Pair<Integer, Integer> getDimensions() {
            return this.properties.getDimensions();
        }

        @Override
        public @NotNull Key getFont() {
            return this.properties.getFont();
        }

        @Override
        public void setAlignment(@NotNull final Alignment alignment) {
            this.properties.setAlignment(alignment);
        }

        @Override
        public void setPosition(final int x, final int y) {
            this.properties.setPosition(x, y);
        }

        @Override
        public void setDimensions(final int width, final int height) {
            this.properties.setDimensions(width, height);
        }

        @Override
        public void setFont(@NotNull final Key font) {
            this.properties.setFont(font);
        }
    }
}
