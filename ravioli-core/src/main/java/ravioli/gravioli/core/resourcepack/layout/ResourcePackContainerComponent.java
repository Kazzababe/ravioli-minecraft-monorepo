package ravioli.gravioli.core.resourcepack.layout;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ResourcePackContainerComponent extends ResourcePackComponent {
    /**
     * Add a child {@link ResourcePackComponent} to this container element.
     *
     * @param child     The child to add
     */
    void addChild(@NotNull ResourcePackComponent child);

    /**
     * Remove a child {@link ResourcePackComponent} from this container element.
     *
     * @param child     The child to remove
     */
    void removeChild(@NotNull ResourcePackComponent child);

    /**
     * Get all {@link ResourcePackComponent} children in this container element.
     *
     * @return      The children of this container element
     */
    @NotNull Collection<@NotNull ResourcePackComponent> children();
}
