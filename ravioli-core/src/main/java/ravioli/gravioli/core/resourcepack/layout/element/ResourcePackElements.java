package ravioli.gravioli.core.resourcepack.layout.element;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public final class ResourcePackElements {
    public enum Generic implements ResourcePackElement {
        LARGE_BLACK_BOX ('\uE001', 4096 * 2, Key.key("ravioli", "default")),
        BLANK_SLATE_54 ('\uE002', 162, Key.key("ravioli", "default")),
        DIALOGUE_BOX ('\uE003', 305, Key.key("ravioli", "default"));

        private final char character;
        private final int width;
        private final Key font;

        Generic(final char character, final int width, @NotNull final Key font) {
            this.character = character;
            this.width = width;
            this.font = font;
        }

        @Override
        public char character() {
            return this.character;
        }

        @Override
        public int width() {
            return this.width;
        }

        @Override
        public @NotNull Key font() {
            return this.font;
        }
    }

    public enum Icons implements ResourcePackElement {
        COINS ('\uE001', 16, Key.key("ravioli", "icons/-4"));

        private final char character;
        private final int width;
        private final Key font;

        Icons(final char character, final int width, @NotNull final Key font) {
            this.character = character;
            this.width = width;
            this.font = font;
        }

        @Override
        public char character() {
            return this.character;
        }

        @Override
        public int width() {
            return this.width;
        }

        @Override
        public @NotNull Key font() {
            return this.font;
        }
    }
}
