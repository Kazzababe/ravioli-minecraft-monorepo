package ravioli.gravioli.dialogue.model;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record DialogueGroup(@NotNull Map<String, ? extends Dialogue> dialogues) {}
