package ravioli.gravioli.core.resourcepack.layout.component;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.layout.AbstractResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;

public class RawComponent extends AbstractResourcePackComponent {
    private final Component component;

    private RawComponent(@NotNull final Component component, @NotNull final ResourcePackComponentProperties properties) {
        super(properties);

        this.component = component;
    }

    @Override
    public @Nullable Component create() {
        return this.component;
    }

    public static final class Builder {
        private final ResourcePackComponentProperties properties;

        private Component component;

        public Builder(@NotNull final Component component, final int width) {
            this.component = component;
            this.properties  = new ResourcePackComponentProperties();

            this.properties.setDimensions(width, 0);
        }

        public @NotNull Builder width(final int width) {
            this.properties.setDimensions(width, 0);

            return this;
        }

        public @NotNull Builder offset(final int offset) {
            this.properties.setPosition(offset, 0);

            return this;
        }

        public @NotNull Builder component(@NotNull final Component component) {
            this.component = component;

            return this;
        }

        public @NotNull RawComponent create() {
            return new RawComponent(this.component, this.properties);
        }
    }
}
