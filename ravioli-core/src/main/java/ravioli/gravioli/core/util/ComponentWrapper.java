package ravioli.gravioli.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentWrapper {
    /**
     * Wrap a component at the specified wrap length.
     * Note: Does not support wrap translatable components and will not wrap internal components such as those in
     *       hover events.
     *
     * @param originalComponent The component to wrap
     * @param wrapLength The length to wrap at
     *
     * @return A list of all new lines of the component.
     */
    @ApiStatus.Experimental
    public static @NotNull List<Component> wrap(@NotNull final Component originalComponent, final int wrapLength) {
        return new ComponentWrapper(originalComponent, wrapLength).wrap();
    }

    private final Component originalComponent;
    private final int wrapLength;

    private List<Component> outputComponents;

    private ComponentWrapper(@NotNull final Component originalComponent, final int wrapLength) {
        this.originalComponent = originalComponent;
        this.wrapLength = wrapLength;
    }

    private TextComponent currentComponent;
    private String currentComponentText;

    public @NotNull List<Component> wrap() {
        this.outputComponents = new ArrayList<>();
        this.currentComponent = this.originalComponent instanceof final TextComponent textComponent ?
            textComponent.children(Collections.emptyList()) :
            Component.empty().append(this.originalComponent.children(Collections.emptyList()));
        this.currentComponentText = "";

        this._wrap(this.originalComponent, this.originalComponent.style(), this.originalComponent.color());

        if (Component.IS_NOT_EMPTY.test(this.currentComponent)) {
            this.outputComponents.add(this.currentComponent);
        }
        return this.outputComponents;
    }

    private void _wrap(@NotNull Component component,
                       @NotNull final Style parentStyle,
                       @Nullable final TextColor parentColor) {
        final List<Component> children = component.children();
        final Style style = parentStyle.merge(component.style());
        final TextColor color = component.color() == null ?
            parentColor :
            component.color();

        component = component.children(Collections.emptyList())
            .style(style)
            .color(color);

        if (component instanceof final TextComponent textComponent) {
            final String content = this.currentComponentText + textComponent.content();
            int start = 0;

            for (int i = 0, length = 0; i < content.length(); i++, length++) {
                final char charAt = content.charAt(i);

                if (length >= this.wrapLength && Character.isWhitespace(charAt)) {
                    final Component newComponent;
                    final String substring = content.substring(start, i).stripLeading();

                    if (this.currentComponentText.isBlank()) {
                        newComponent = textComponent.content(substring);
                    } else {
                        if (i < this.currentComponentText.length()) {
                            newComponent = this.currentComponent.content(substring);
                        } else {
                            String latter = this.safeSubstring(content, this.currentComponentText.length(), i);

                            if (latter.isBlank()) {
                                newComponent = Component.empty().append(this.currentComponent);
                            } else {
                                if (this.currentComponentText.isBlank()) {
                                    newComponent = Component.empty()
                                        .append(textComponent.content(latter.stripLeading()));
                                } else {
                                    newComponent = Component.empty()
                                        .append(this.currentComponent)
                                        .append(textComponent.content(latter));
                                }
                            }
                        }
                    }
                    this.outputComponents.add(newComponent);
                    this.currentComponent = textComponent.content("");
                    this.currentComponentText = "";

                    length = 0;
                    start = i;
                }
            }
            final String remaining = content.substring(start).stripLeading();

            if (remaining.isBlank()) {
                this.currentComponent = this.currentComponent.content("")
                    .children(Collections.emptyList());
            } else {
                if (start == 0) {
                    if (this.currentComponentText.isBlank()) {
                        this.currentComponent = textComponent;
                    } else {
                        this.currentComponent = Component.empty()
                            .append(this.currentComponent)
                            .append(textComponent);
                    }
                } else {
                    this.currentComponent = Component.empty()
                        .append(this.currentComponent.content(
                            this.safeSubstring(remaining, start, this.currentComponentText.length())
                        ))
                        .append(textComponent.content(
                            this.safeSubstring(remaining, this.currentComponentText.length())
                        ));
                }
            }
            this.currentComponentText = remaining;
        }
        for (final Component child : children) {
            this._wrap(child, style, color);
        }
    }

    private @NotNull String safeSubstring(@NotNull final String string, final int start) {
        try {
            return string.substring(start);
        } catch (final IndexOutOfBoundsException e) {
            return "";
        }
    }

    private @NotNull String safeSubstring(@NotNull final String string, final int start, final int end) {
        try {
            return string.substring(start, end);
        } catch (final IndexOutOfBoundsException e) {
            return "";
        }
    }
}
