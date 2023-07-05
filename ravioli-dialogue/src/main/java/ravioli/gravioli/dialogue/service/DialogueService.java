package ravioli.gravioli.dialogue.service;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.action.DialogueAction;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.requirement.DialogueRequirement;
import ravioli.gravioli.dialogue.source.DialogueSource;
import ravioli.gravioli.dialogue.text.DialogueComponent;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DialogueService {
    void attachDialogue(@NotNull Entity entity, @NotNull DialogueGroup dialogue);

    void unattachDialogue(@NotNull Entity entity, @NotNull DialogueGroup dialogueGroup);

    @NotNull List<@NotNull Entity> getAttachedEntities(@NotNull DialogueGroup dialogueGroup);

    @NotNull DialogueGroup loadDialogue(@NotNull File file) throws IOException;

    boolean processDialogue(@NotNull Player viewer,
                            @NotNull DialogueGroup dialogueGroup,
                            @NotNull DialogueSource dialogueSource);
}
