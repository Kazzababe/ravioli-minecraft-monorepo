package ravioli.gravioli.dialogue.source;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DialogueSource {
    DialogueSource NO_SOURCE = player -> true;

    boolean isValid(@NotNull Player player);
}
