package ravioli.gravioli.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JsonUtil {
    public static @Nullable String getString(@NotNull final JsonObject jsonObject, @NotNull final String property) {
        if (!jsonObject.has("property")) {
            return null;
        }
        final JsonElement jsonElement = jsonObject.get("property");

        if (jsonElement.isJsonNull()) {
            return null;
        }
        return jsonElement.getAsString();
    }
}
