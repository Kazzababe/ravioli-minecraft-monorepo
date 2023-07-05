package ravioli.gravioli.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public final class JsonObjectBuilder {
    private final JsonObject jsonObject;

    public JsonObjectBuilder() {
        this.jsonObject = new JsonObject();
    }

    public JsonObjectBuilder addProperty(@NotNull final String propertyName, @NotNull final String propertyValue) {
        this.jsonObject.addProperty(propertyName, propertyValue);

        return this;
    }

    public JsonObjectBuilder addProperty(@NotNull final String propertyName, @NotNull final Number propertyValue) {
        this.jsonObject.addProperty(propertyName, propertyValue);

        return this;
    }

    public JsonObjectBuilder addProperty(@NotNull final String propertyName, @NotNull final Boolean propertyValue) {
        this.jsonObject.addProperty(propertyName, propertyValue);

        return this;
    }

    public JsonObjectBuilder addProperty(@NotNull final String propertyName, @NotNull final Character propertyValue) {
        this.jsonObject.addProperty(propertyName, propertyValue);

        return this;
    }

    public JsonObjectBuilder add(@NotNull final String propertyName, @NotNull final JsonElement propertyValue) {
        this.jsonObject.add(propertyName, propertyValue);

        return this;
    }

    public JsonObject build() {
        return this.jsonObject;
    }
}
