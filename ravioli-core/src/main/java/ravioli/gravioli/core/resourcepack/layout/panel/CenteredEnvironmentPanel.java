package ravioli.gravioli.core.resourcepack.layout.panel;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CenteredEnvironmentPanel extends Panel {

    private CenteredEnvironmentPanel(@NotNull final ResourcePackComponentProperties properties) {
        super(properties);
    }

    public static class Builder {
        private final List<ResourcePackComponent> children;
        private final ResourcePackComponentProperties properties;

        public Builder() {
            this.children = new ArrayList<>();
            this.properties = new ResourcePackComponentProperties();
        }

        public @NotNull Builder child(@NotNull final ResourcePackComponent component) {
            this.children.add(component);

            return this;
        }

        public @NotNull Builder width(final int width) {
            this.properties.setDimensions(width, 0);
            this.properties.setPosition(-width / 2 + 1, 0);

            return this;
        }

        public @NotNull Builder alignment(@NotNull final Alignment alignment) {
            this.properties.setAlignment(alignment);

            return this;
        }

        public @NotNull Component create() {
            final Panel panel = new CenteredEnvironmentPanel(this.properties);

            this.children.forEach(panel::addChild);

            return Objects.requireNonNullElse(panel.create(), Component.empty());
        }
    }
}
