package ravioli.gravioli.core.resourcepack.layout.panel;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChestEnvironmentPanel extends Panel {
    public static final int CHEST_WIDTH = 176;
    public static final int CHEST_OFFSET = -8;

    private ChestEnvironmentPanel(@NotNull final ResourcePackComponentProperties properties) {
        super(properties);
    }

    @Override
    protected boolean resetOffset() {
        return false;
    }

    public static class Builder {
        private final List<ResourcePackComponent> children;
        private final ResourcePackComponentProperties properties;

        public Builder() {
            this.children = new ArrayList<>();
            this.properties = new ResourcePackComponentProperties();

            this.properties.setPosition(CHEST_OFFSET, 0);
            this.properties.setDimensions(CHEST_WIDTH, 0);
        }

        public @NotNull Builder child(@NotNull final ResourcePackComponent component) {
            this.children.add(component);

            return this;
        }

        public @NotNull Builder alignment(@NotNull final Alignment alignment) {
            this.properties.setAlignment(alignment);

            return this;
        }

        public @NotNull Component create() {
            final Panel panel = new ChestEnvironmentPanel(this.properties);

            this.children.forEach(panel::addChild);

            return Objects.requireNonNullElse(panel.create(), Component.empty());
        }
    }
}
