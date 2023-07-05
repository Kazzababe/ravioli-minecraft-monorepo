package ravioli.gravioli.core.resourcepack;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class Shift {
    public static final Key SPACES_KEY = Key.key("space", "default");

    public static @NotNull Component component(final int offset) {
        return Component.translatable("space." + offset)
            .font(SPACES_KEY);
    }
}
