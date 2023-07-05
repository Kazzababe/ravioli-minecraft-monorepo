package ravioli.gravioli.dialogue.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DialogueKey(@NotNull UUID uuid, @NotNull String dialogueId) {}
