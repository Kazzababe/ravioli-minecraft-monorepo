package ravioli.gravioli.dialogue.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.dialogue.model.conversation.Conversation;
import ravioli.gravioli.dialogue.model.input.Input;
import ravioli.gravioli.dialogue.service.RavioliDialogueService;

public class DialogueInputListeners extends PacketAdapter {
    private final RavioliDialogueService dialogueService;

    public DialogueInputListeners(@NotNull final RavioliDialogueService dialogueService, @NotNull final Plugin plugin) {
        super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG);

        this.dialogueService = dialogueService;
    }

    @Override
    public void onPacketReceiving(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();
        final Conversation conversation = this.dialogueService.getCurrentConversation(player.getUniqueId())
            .orElse(null);

        if (conversation == null) {
            return;
        }
        final PacketContainer packet = event.getPacket();
        final int statusId = packet.getIntegers().read(0);

        if (statusId == 0) {
            conversation.onInput(Input.DROP_ITEM);
        }
    }
}
