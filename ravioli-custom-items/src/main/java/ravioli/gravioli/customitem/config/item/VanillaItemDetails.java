package ravioli.gravioli.customitem.config.item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public record VanillaItemDetails(@Nullable String displayName, @NotNull List<String> lore) {
    public static @NotNull VanillaItemDetails EMPTY = new VanillaItemDetails(null, Collections.emptyList());
}
