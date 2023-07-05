package ravioli.gravioli.core.resourcepack.layout;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractResourcePackContainerComponent extends AbstractResourcePackComponent implements ResourcePackContainerComponent {
    private final List<ResourcePackComponent> children;

    public AbstractResourcePackContainerComponent(@NotNull final ResourcePackComponentProperties properties) {
        super(properties);

        this.children = new ArrayList<>();
    }

    public final void addChild(@NotNull final ResourcePackComponent child) {
        this.children.add(child);

        if (child instanceof final AbstractResourcePackComponent component) {
            component.parent(this);
        }
    }

    public final void removeChild(@NotNull final ResourcePackComponent child) {
        this.children.remove(child);
    }

    @Override
    public @NotNull Collection<@NotNull ResourcePackComponent> children() {
        return ImmutableList.copyOf(this.children);
    }
}
