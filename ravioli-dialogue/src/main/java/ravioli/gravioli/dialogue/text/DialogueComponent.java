package ravioli.gravioli.dialogue.text;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DialogueComponent {
    default boolean skip() {
        return false;
    }

    default void perform(@NotNull Player viewer) {}

    @NotNull
    String getText();

    @NotNull
    Component getFormattedText(@NotNull Player viewer);
}
