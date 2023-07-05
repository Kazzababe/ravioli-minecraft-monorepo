package ravioli.gravioli.dialogue.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DialogueStates(@NotNull List<DialogueState> validDialogueStates) {
    public record DialogueState(long dialogueIndex) {}
}
