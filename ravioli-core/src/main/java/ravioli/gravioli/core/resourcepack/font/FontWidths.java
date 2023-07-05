package ravioli.gravioli.core.resourcepack.font;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class FontWidths {
    private static final Logger LOGGER = LoggerFactory.getLogger(FontWidths.class);

    private static final Key DEFAULT_FONT = Key.key("minecraft", "default");
    private static final Map<Key, FontWidth> FONT_WIDTHS = new HashMap<>();

    public static void registerFontWidth(@NotNull final Key font, @NotNull final FontWidth fontWidth) {
        FONT_WIDTHS.put(font, fontWidth);

        LOGGER.info("Registered font \"" + font.asString() + "\".");
    }

    public static int width(@NotNull final String text) {
        return width(text, false);
    }

    public static int width(@NotNull final String text, final boolean bold) {
        return width(text, bold, DEFAULT_FONT);
    }

    public static int width(@NotNull final String text, @NotNull final Key font) {
        return width(text, false, font);
    }

    public static int width(@NotNull final String text, final boolean bold, @NotNull final Key font) {
        final FontWidth fontWidth = Objects.requireNonNull(FONT_WIDTHS.get(font), "No font-widths registered for font: " + font);
        final int length = text.codePointCount(0, text.length());
        int width = 0;

        for (int i = 0; i < length; i++) {
            final char character = (char) text.codePointAt(i);

            width += fontWidth.getCharacterWidth(character) + (bold ? 2 : 1);
        }
        return width;
    }
}
