package ravioli.gravioli.core.resourcepack.layout;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;

public interface ResourcePackComponent {

    /**
     * Gets the properties of this resource pack component.
     *
     * @return The properties of this resource pack component.
     */
    @NotNull ResourcePackComponentProperties properties();

    /**
     * Gets the parent resource pack component, if one exists.
     *
     * @return The parent resource pack component, or null if there is no parent.
     */
    @Nullable ResourcePackContainerComponent parent();

    /**
     * Converts the resource pack element to a {@link Component}..
     *
     * @return The converted Component.
     */
    @Nullable Component create();
}
