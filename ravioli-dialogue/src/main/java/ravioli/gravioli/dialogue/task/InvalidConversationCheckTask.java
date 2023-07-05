package ravioli.gravioli.dialogue.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.model.conversation.Conversation;
import ravioli.gravioli.dialogue.service.DialogueService;
import ravioli.gravioli.dialogue.service.RavioliDialogueService;

public class InvalidConversationCheckTask implements Runnable {
    private final RavioliDialogueService dialogueService;

    public InvalidConversationCheckTask(@NotNull final RavioliDialogueService dialogueService) {
        this.dialogueService = dialogueService;
    }

    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final Conversation currentConversation = this.dialogueService.getCurrentConversation(player.getUniqueId())
                .orElse(null);

            if (currentConversation == null) {
                continue;
            }
            if (currentConversation.source().isValid(player)) {
                continue;
            }
            currentConversation.stop();

            this.dialogueService.clearCurrentConversation(player.getUniqueId());
        }
    }
}
