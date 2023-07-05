package ravioli.gravioli.core.resourcepack.layout.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.core.resourcepack.Shift;
import ravioli.gravioli.core.resourcepack.font.FontWidths;
import ravioli.gravioli.core.resourcepack.layout.AbstractResourcePackComponent;
import ravioli.gravioli.core.resourcepack.layout.properties.ResourcePackComponentProperties;
import ravioli.gravioli.core.resourcepack.layout.properties.model.Alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link Label} is just another representation for a {@link String}. In order to provide flexibility for
 * text where different parts of the text have different styling, you can supply the contents of the StringElement
 * in pieces where each piece can have its own styling. To create a StringElement that says "Hello World" where "Hello"
 * is green and "World" is red could be like this:
 *
 * <pre>{@code
 * new StringElement.Builder("Hello ", "World")
 *   .font(Key.key("my", "font"))
 *   .contentModifiers(
 *     (builder) -> builder.color(NamedTextColor.GREEN),
 *     (builder) -> builder.color(NamedTextColor.RED)
 *   )
 *   .create()
 * }</pre>
 */
public class Label extends AbstractResourcePackComponent {
    private final List<String> contents;
    private final List<Consumer<TextComponent.Builder>> contentModifiers;

    private Label(@NotNull final List<String> contents,
                  @NotNull final List<Consumer<TextComponent.Builder>> contentModifiers,
                  @NotNull final ResourcePackComponentProperties properties) {
        super(properties);

        this.contents = contents;
        this.contentModifiers = contentModifiers;
    }

    @Override
    public @NotNull Component create() {
        final Pair<Integer, Integer> position = this.properties().getPosition();
        final Pair<Integer, Integer> dimensions = this.properties().getDimensions();
        Component component = null;

        for (int i = 0; i < this.contents.size(); i++) {
            final String content = this.contents.get(i);
            final TextComponent.Builder builder = Component.text(content)
                .toBuilder()
                .font(this.properties().getFont());

            if (i < this.contentModifiers.size()) {
                this.contentModifiers.get(i).accept(builder);
            }
            final Component contentComponent = builder.build();

            if (component == null) {
                component = contentComponent;
            } else {
                component = component.append(contentComponent);
            }
        }
        if (component != null) {
            component = component.append(Shift.component(-(dimensions.getLeft() + 1)));

            if (position.getLeft() != 0) {
                final int xOffset = position.getLeft();

                component = Shift.component(xOffset)
                    .append(component)
                    .append(Shift.component(-xOffset));
            }
        }
        return Objects.requireNonNullElse(component, Component.empty());
    }

    public static class Builder {
        private final List<String> contents;
        private final List<Consumer<TextComponent.Builder>> contentModifiers;
        private final ResourcePackComponentProperties properties;

        public Builder(@NotNull final String... contents) {
            this.contents = new ArrayList<>(List.of(contents));
            this.contentModifiers = new ArrayList<>();
            this.properties = new ResourcePackComponentProperties();
        }

        public @NotNull Builder contents(@NotNull final String... contents) {
            this.contents.addAll(List.of(contents));

            return this;
        }

        @SafeVarargs
        public final @NotNull Builder contentModifiers(@NotNull final Consumer<TextComponent.Builder>... contentModifiers) {
            this.contentModifiers.addAll(List.of(contentModifiers));

            return this;
        }

        public @NotNull Builder font(@NotNull final Key font) {
            this.properties.setFont(font);

            return this;
        }

        public @NotNull Builder alignment(@NotNull final Alignment alignment) {
            this.properties.setAlignment(alignment);

            return this;
        }

        public @NotNull Label create() {
            this.properties.setDimensions(this.calculateWidth(), 0);

            return new Label(this.contents, this.contentModifiers, this.properties);
        }

        private int calculateWidth() {
            int width = this.contents.isEmpty() ? 0 : -1;

            for (int i = 0; i < this.contents.size(); i++) {
                final String content = this.contents.get(i);
                boolean bold = false;
                Key font = this.properties.getFont();

                if (i < this.contentModifiers.size()) {
                    final TextComponent.Builder builder = Component.text(content)
                        .toBuilder();
                    final Consumer<TextComponent.Builder> modifier = this.contentModifiers.get(i);

                    modifier.accept(builder);

                    final TextComponent build = builder.build();

                    if (build.hasDecoration(TextDecoration.BOLD)) {
                        bold = true;
                    }
                    if (build.font() != null) {
                        font = build.font();
                    }
                }
                width += FontWidths.width(content, bold, font);
            }
            return width;
        }
    }
}
