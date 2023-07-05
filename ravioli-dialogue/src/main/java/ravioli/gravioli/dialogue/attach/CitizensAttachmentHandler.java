package ravioli.gravioli.dialogue.attach;

import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.service.DialogueService;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CitizensAttachmentHandler implements AttachmentHandler, Listener {
    private final File rootFolder;
    private final DialogueService dialogueService;
    private final Map<Integer, DialogueGroup> dialogueEntries = new HashMap<>();

    public CitizensAttachmentHandler(@NotNull final File rootFolder) {
        this.rootFolder = rootFolder;
        this.dialogueService = Objects.requireNonNull(Platform.loadService(DialogueService.class));
    }

    @EventHandler
    private void onNpcSpawn(@NotNull final NPCSpawnEvent event) {
        final NPC npc = event.getNPC();
        final int npcId = npc.getId();
        final DialogueGroup dialogue = this.dialogueEntries.get(npcId);

        if (dialogue == null) {
            return;
        }
        this.dialogueService.attachDialogue(npc.getEntity(), dialogue);
    }

    @EventHandler
    private void onNpcDespawn(@NotNull final NPCDespawnEvent event) {
        final NPC npc = event.getNPC();
        final int npcId = npc.getId();
        final DialogueGroup dialogue = this.dialogueEntries.get(npcId);

        if (dialogue == null) {
            return;
        }
        this.dialogueService.unattachDialogue(npc.getEntity(), dialogue);
    }

    @Override
    public void registerAttachment(@NotNull final ConfigurationSection section) {
        final int id = section.getInt("npc-id", -1);

        if (id == -1) {
            return;
        }
        final String dialoguePath = Objects.requireNonNull(section.getString("dialogue-file"));
        final File dialogueFile = new File(this.rootFolder, dialoguePath);

        try {
            final DialogueGroup dialogue = this.dialogueService.loadDialogue(dialogueFile);

            this.dialogueEntries.put(id, dialogue);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
