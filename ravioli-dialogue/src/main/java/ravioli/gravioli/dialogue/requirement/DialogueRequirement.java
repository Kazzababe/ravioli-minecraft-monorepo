package ravioli.gravioli.dialogue.requirement;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DialogueRequirement {
    int getPriority();

    boolean valid(@NotNull Player player);
}
