package ravioli.gravioli.dialogue.config;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.model.Dialogue;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.model.conversation.BukkitConversation;
import ravioli.gravioli.dialogue.model.conversation.Conversation;
import ravioli.gravioli.dialogue.source.DialogueSource;
import ravioli.gravioli.dialogue.text.DialogueComponent;

public class ConversationConfig {
    private final DialogueComponent[] dialogueComponents;

    public ConversationConfig(@NotNull final DialogueComponent... dialogueComponents) {
        this.dialogueComponents = dialogueComponents;
    }

    public @NotNull Conversation createConversation(@NotNull final Player player,
                                                    @NotNull final DialogueGroup parentDialogueGroup,
                                                    @NotNull final Dialogue parentDialogue,
                                                    @NotNull final DialogueSource dialogueSource) {
        return new BukkitConversation(player, dialogueSource, parentDialogueGroup, parentDialogue, this.dialogueComponents);
    }
}
