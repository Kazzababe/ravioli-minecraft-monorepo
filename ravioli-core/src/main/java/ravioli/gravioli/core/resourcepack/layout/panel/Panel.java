package ravioli.gravioli.core.resourcepack.layout.panel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.Shift;
import ravioli.gravioli.core.resourcepack.layout.AbstractResourcePackContainerComponent;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

import java.util.*;

public class Panel extends AbstractResourcePackContainerComponent {
    protected Panel(@NotNull final ResourcePackComponentProperties properties) {
        super(properties);
    }

    @Override
    public @Nullable Component create() {
        final int width = this.properties().getDimensions().getLeft();
        final Collection<ResourcePackComponent> children = this.children();
        Component finalComponent = this.createPanelComponent();

        for (final ResourcePackComponent child : children) {
            final int childWidth = child.properties().getDimensions().getLeft();
            final Alignment alignment = Objects.requireNonNullElse(child.properties().getAlignment(), Alignment.LEFT);
            Component component = child.create();

            if (component == null) {
                continue;
            }
            switch (alignment) {
                case CENTER -> {
                    final int halfWidth = childWidth / 2;
                    final int panelHalfWidth = width / 2;

                    component = Shift.component(panelHalfWidth - halfWidth)
                        .append(component)
                        .append(Shift.component(halfWidth - panelHalfWidth));
                }
                case RIGHT -> {
                    component = Shift.component(width - childWidth)
                        .append(component)
                        .append(Shift.component(childWidth - width));
                }
            }
            if (finalComponent == null) {
                finalComponent = component;
            } else {
                finalComponent = finalComponent.append(component);
            }
        }
        if (finalComponent == null) {
            return null;
        }
        final int xOffset = this.properties().getPosition().getLeft();

        if (xOffset != 0) {
            finalComponent = Shift.component(xOffset)
                .append(finalComponent);

            if (this.resetOffset()) {
                finalComponent = finalComponent.append(Shift.component(-xOffset));
            }
        }
        return this.mergeSpacingComponents(finalComponent);
    }

    protected @Nullable Component createPanelComponent() {
        return null;
    }

    protected boolean resetOffset() {
        return true;
    }

    private @NotNull Component mergeSpacingComponents(@NotNull final Component root) {
        final List<Component> flattenedComponents = new ArrayList<>();

        this.flattenComponents(root, flattenedComponents);

        if (flattenedComponents.isEmpty()) {
            return root;
        }
        final List<Component> mergedComponents = new ArrayList<>();
        Component previous = null;

        for (Component component : flattenedComponents) {
            if (previous instanceof final TranslatableComponent previousTranslatable &&
                component instanceof final TranslatableComponent currentTranslatable) {
                if (Shift.SPACES_KEY.equals(previousTranslatable.font()) && Shift.SPACES_KEY.equals(currentTranslatable.font())) {
                    final int previousSpace = this.parseSpacing(previousTranslatable.key());
                    final int currentSpace = this.parseSpacing(currentTranslatable.key());

                    previous = previousTranslatable.key("space." + (previousSpace + currentSpace));
                } else {
                    mergedComponents.add(previous);

                    previous = component;
                }
            } else {
                if (previous != null) {
                    mergedComponents.add(previous);
                }
                previous = component;
            }
        }
        mergedComponents.add(previous);

        return this.reconstructComponent(mergedComponents);
    }

    private int parseSpacing(@NotNull final String key) {
        final String start = "space.";

        return Integer.parseInt(key.substring(start.length()));
    }

    private void flattenComponents(@Nullable final Component root, @NotNull final List<Component> components) {
        if (root == null) {
            return;
        }
        components.add(root.children(Collections.emptyList()));

        if (root.children().isEmpty()) {
            return;
        }
        for (final Component component : root.children()) {
            this.flattenComponents(component, components);
        }
    }

    private @NotNull Component reconstructComponent(@NotNull final List<Component> components) {
        Component finalComponent = null;

        for (final Component component : components) {
            if (finalComponent == null) {
                finalComponent = component;
            } else {
                finalComponent = finalComponent.append(component);
            }
        }
        return Objects.requireNonNull(finalComponent);
    }

    public static class Builder {
        private final List<ResourcePackComponent> children;
        private final ResourcePackComponentProperties properties;

        public Builder(final int width, final int offset) {
            this.children = new ArrayList<>();
            this.properties = new ResourcePackComponentProperties();

            this.properties.setDimensions(width, 0);
            this.properties.setPosition(offset, 0);
        }

        public Builder(final int width) {
            this(width, 0);
        }

        public @NotNull Builder dimensions(final int width, final int height) {
            this.properties.setDimensions(width, height);

            return this;
        }

        public @NotNull Builder position(final int x, final int y) {
            this.properties.setPosition(x, y);

            return this;
        }

        public @NotNull Builder child(@NotNull final ResourcePackComponent component) {
            this.children.add(component);

            return this;
        }

        public @NotNull Builder alignment(@NotNull final Alignment alignment) {
            this.properties.setAlignment(alignment);

            return this;
        }

        public @NotNull Panel create() {
            final Panel panel = new Panel(this.properties);

            this.children.forEach(panel::addChild);

            return panel;
        }
    }
}
