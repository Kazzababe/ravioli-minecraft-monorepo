package ravioli.gravioli.dialogue.model.conversation;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.dialogue.model.Dialogue;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.model.input.Input;
import ravioli.gravioli.dialogue.service.DialogueService;
import ravioli.gravioli.dialogue.source.DialogueSource;
import ravioli.gravioli.dialogue.text.DialogueComponent;
import ravioli.gravioli.dialogue.text.animation.TypingDialogueAnimation;

import java.time.Duration;
import java.util.Objects;

public class BukkitConversation implements Conversation {
    private final DialogueComponent[] dialogueComponents;
    private final DialogueSource dialogueSource;
    private final DialogueGroup parentDialogueGroup;
    private final Dialogue parentDialogue;
    private final Player player;

    private int dialogueIndex;
    private TypingDialogueAnimation typingDialogueAnimation;

    public BukkitConversation(@NotNull final Player player,
                              @NotNull final DialogueSource dialogueSource,
                              @NotNull final DialogueGroup parentDialogueGroup,
                              @NotNull final Dialogue parentDialogue,
                              @NotNull final DialogueComponent... dialogueComponents) {
        this.dialogueComponents = dialogueComponents;
        this.dialogueSource = dialogueSource;
        this.parentDialogueGroup = parentDialogueGroup;
        this.parentDialogue = parentDialogue;
        this.player = player;
    }

    @Override
    public void stop() {
        if (this.typingDialogueAnimation != null) {
            this.typingDialogueAnimation.exit();
        }
    }

    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull DialogueGroup parentDialogueGroup() {
        return this.parentDialogueGroup;
    }

    @Override
    public @NotNull Dialogue parentDialogue() {
        return this.parentDialogue;
    }

    @Override
    public @NotNull DialogueSource source() {
        return this.dialogueSource;
    }

    @Override
    public @NotNull NextResult next() {
        if (this.typingDialogueAnimation != null) {
            if (this.typingDialogueAnimation.isRunning()) {
                this.typingDialogueAnimation.skipToEnd();

                return NextResult.DO_NOTHING;
            } else {
                this.typingDialogueAnimation.exit();
                this.typingDialogueAnimation = null;
            }
        }
        if (this.dialogueIndex >= this.dialogueComponents.length) {
            return NextResult.PROCEED;
        }
        final DialogueComponent dialogueComponent = this.dialogueComponents[this.dialogueIndex++];

        if (dialogueComponent.skip()) {
            return this.next();
        }
        final String text = dialogueComponent.getText();
        final long milliseconds = 50L * text.length();

        this.typingDialogueAnimation = new TypingDialogueAnimation(text, Duration.ofMillis(milliseconds), player);
        this.typingDialogueAnimation.start();

        return NextResult.DO_NOTHING;
    }

    @Override
    public boolean onInput(@NotNull final Input input) {
        if (input == Input.DROP_ITEM) {
            Objects.requireNonNull(Platform.loadService(DialogueService.class))
                .processDialogue(this.player, this.parentDialogueGroup, this.dialogueSource);

            return true;
        }
        return false;
    }
}
