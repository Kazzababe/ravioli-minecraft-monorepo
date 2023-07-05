package ravioli.gravioli.dialogue.action;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DialogueAction {
    void perform(@NotNull Player viewer);
}
