package ravioli.gravioli.core.resourcepack.font;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FontWidth {
    private static final int DEFAULT_WIDTH = 4;

    private final Map<Character, Integer> widthMap = new HashMap<>();

    public FontWidth(@NotNull final JsonObject jsonObject) {
        final JsonArray charactersArray = jsonObject.getAsJsonArray("characters");
        final JsonArray widthsArray = jsonObject.getAsJsonArray("widths");
        final Character[] characters = this.parseCharacters(charactersArray);
        final Integer[] widths = this.parseWidths(widthsArray);

        for (int i = 0; i < characters.length; i++) {
            final char character = characters[i];
            final int width = i < widths.length ? widths[i] : DEFAULT_WIDTH;

            this.widthMap.put(character, width);
        }
    }

    private Character[] parseCharacters(@NotNull final JsonArray jsonArray) {
        final List<Character> characters = new ArrayList<>();

        for (final JsonElement jsonElement : jsonArray) {
            final String line = jsonElement.getAsString();
            final int length = line.codePointCount(0, line.length());

            for (int i = 0; i < length; i++) {
                final int characterCode = line.codePointAt(i);

                characters.add((char) characterCode);
            }
        }
        return characters.toArray(Character[]::new);
    }

    private Integer[] parseWidths(@NotNull final JsonArray jsonArray) {
        final List<Integer> widths = new ArrayList<>();

        for (final JsonElement jsonElement : jsonArray) {
            final int width = jsonElement.getAsInt();

            widths.add(width);
        }
        return widths.toArray(Integer[]::new);
    }

    public int getCharacterWidth(final char character) {
        return this.widthMap.getOrDefault(character, DEFAULT_WIDTH);
    }
}
