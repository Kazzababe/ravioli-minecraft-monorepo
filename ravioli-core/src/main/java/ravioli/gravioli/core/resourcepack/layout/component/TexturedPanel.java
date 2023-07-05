package ravioli.gravioli.core.resourcepack.layout.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.core.resourcepack.Shift;
import ravioli.gravioli.core.resourcepack.layout.ResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.element.ResourcePackElement;
import ravioli.gravioli.core.resourcepack.layout.panel.Panel;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@link TexturedPanel} is just a panel with contents inside of it. To create a basic button with text inside of it, you
 * could do something like the below:
 *
 * <pre>{@code
 * new TexturedPanel.Builder('\uE001', 100, Key.key("my", "font"))
 *   .child(new Label.Builder("This is a button")
 *   .alignment(Alignment.CENTER)
 *   .create()
 * }</pre>
 *
 * This would create a button with the following parameters:
 * <p>- A background image of whatever the character '\uE001' is mapped to of 100px width</p>
 * <p>- The text "This is a button" centered in the button itself</p>
 */
public class TexturedPanel extends Panel {
    private final char content;
    private final Consumer<TextComponent.Builder> contentModifier;

    private TexturedPanel(final char content,
                          @Nullable final Consumer<TextComponent.Builder> contentModifier,
                          final ResourcePackComponentProperties properties) {
        super(properties);

        this.content = content;
        this.contentModifier = contentModifier;
    }

    @Override
    protected @NotNull Component createPanelComponent() {
        final int renderWidth = this.properties().getDimensions().getLeft() + 1;
        final TextComponent.Builder builder = Component.text(this.content)
            .append(Shift.component(-renderWidth))
            .toBuilder()
            .font(this.properties().getFont());

        if (this.contentModifier != null) {
            this.contentModifier.accept(builder);
        }
        return builder.build();
    }

    public static class Builder {
        private final List<ResourcePackComponent> children;
        private final ResourcePackComponentProperties properties;

        private char content;
        private Consumer<TextComponent.Builder> contentModifier;

        public Builder(final char content, final int width, @NotNull final Key font) {
            this.children = new ArrayList<>();
            this.content = content;
            this.properties = new ResourcePackComponentProperties();

            this.properties.setDimensions(width, 0);
            this.properties.setFont(font);
        }

        public Builder(final ResourcePackElement resourcePackElement) {
            this(resourcePackElement.character(), resourcePackElement.width(), resourcePackElement.font());

            this.contentModifier((builder) -> builder.color(NamedTextColor.WHITE));
        }

        public @NotNull Builder content(final char content) {
            this.content = content;

            return this;
        }

        public @NotNull Builder contentModifier(@NotNull final Consumer<TextComponent.Builder> contentModifier) {
            if (this.contentModifier == null) {
                this.contentModifier = contentModifier;
            } else {
                this.contentModifier = (builder) -> {
                    this.contentModifier.accept(builder);
                    contentModifier.accept(builder);
                };
            }
            return this;
        }

        public @NotNull Builder dimensions(final int width, final int height) {
            this.properties.setDimensions(width, height);

            return this;
        }

        public @NotNull Builder child(@NotNull final ResourcePackComponent resourcePackComponent) {
            this.children.add(resourcePackComponent);

            return this;
        }

        public @NotNull Builder font(@NotNull final Key font) {
            this.properties.setFont(font);

            return this;
        }

        public @NotNull Builder position(final int x, final int y) {
            this.properties.setPosition(x, y);

            return this;
        }

        public @NotNull Builder alignment(@NotNull final Alignment alignment) {
            this.properties.setAlignment(alignment);

            return this;
        }

        public @NotNull TexturedPanel create() {
            final TexturedPanel button = new TexturedPanel(this.content, this.contentModifier, this.properties);

            this.children.forEach(button::addChild);

            return button;
        }
    }
}
