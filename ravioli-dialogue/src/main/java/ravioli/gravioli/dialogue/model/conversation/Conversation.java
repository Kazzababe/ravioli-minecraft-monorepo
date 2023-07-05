package ravioli.gravioli.dialogue.model.conversation;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.model.Dialogue;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.model.input.Input;
import ravioli.gravioli.dialogue.source.DialogueSource;

public interface Conversation {
    @NotNull NextResult next();

    boolean onInput(@NotNull Input input);

    void stop();

    @NotNull Player getPlayer();

    @NotNull DialogueGroup parentDialogueGroup();

    @NotNull Dialogue parentDialogue();

    @NotNull DialogueSource source();

    enum NextResult {
        DO_NOTHING,
        PROCEED,
        END;
    }
}
