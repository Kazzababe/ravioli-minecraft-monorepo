package ravioli.gravioli.dialogue.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.data.DialogueDao;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.model.DialogueKey;
import ravioli.gravioli.dialogue.service.RavioliDialogueService;
import ravioli.gravioli.dialogue.source.EntityDialogueSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DialogueListeners implements Listener {
    private final RavioliDialogueService dialogueService;
    private final DialogueDao dialogueDao;

    public DialogueListeners(@NotNull final RavioliDialogueService dialogueService, @NotNull final DialogueDao dialogueDao) {
        this.dialogueService = dialogueService;
        this.dialogueDao = dialogueDao;
    }

    @EventHandler
    private void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
        final UUID uuid = event.getUniqueId();
        final Map<DialogueKey, Integer> stateMap = this.dialogueDao.loadPlayerStates(uuid)
            .join();

        this.dialogueService.setPlayerStates(stateMap);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        this.dialogueService.invalidateUser(player);
    }

    @EventHandler
    private void onEntityInteract(final PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        final Entity entity = event.getRightClicked();
        final UUID entityId = entity.getUniqueId();
        final List<DialogueGroup> dialogueGroup = this.dialogueService.getAttachedDialogue(entityId);

        if (dialogueGroup == null) {
            return;
        }
        final Player player = event.getPlayer();

        for (final DialogueGroup dialogue : dialogueGroup) {
            if (this.dialogueService.processDialogue(player, dialogue, new EntityDialogueSource(entity))) {
                return;
            }
        }
    }
}
