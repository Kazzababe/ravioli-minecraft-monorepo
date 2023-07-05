package ravioli.gravioli.common.data.entity;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for data entities that can be serialized to JSON format. This is used for caching
 * and storing data entities in a way that can be easily converted back into its original format.
 * <p>
 * Any class implementing this interface must also provide a public static deserialize method
 * with a {@link JsonObject} parameter that returns an instance of the implementing class.
 * The signature of this method should be:
 * <pre>{@code
 * public static @NotNull ClassName deserialize(@NotNull JsonObject serializedData)
 * }</pre>
 * where {@code ClassName} is the implementing class's name.
 * <p>
 * The deserialize method is responsible for creating a new instance of the implementing class
 * from the serialized data.
 * <p>
 * Here is an example of how it should look like:
 * <pre>{@code
 * public class ExampleDataEntity implements DataEntity {
 *     @NotNull
 *     @Override
 *     public JsonObject serialize() {
 *         // Your serialization logic here...
 *     }
 *
 *     public static ExampleDataEntity deserialize(@NotNull JsonObject serializedData) {
 *         // Your deserialization logic here...
 *     }
 * }
 * }</pre>
 *
 */
public interface DataEntity {
    /**
     * Return a serialized representation of the data entity.
     *
     * @return a {@link JsonObject} representing the data entity
     */
    @NotNull
    JsonObject serialize();
}
