package ravioli.gravioli.dialogue;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import ravioli.gravioli.command.bukkit.BukkitCommandManager;
import ravioli.gravioli.common.Platform;
import ravioli.gravioli.core.api.RavioliPlugin;
import ravioli.gravioli.core.util.SchedulerUtil;
import ravioli.gravioli.dialogue.action.generic.CommandDialogueAction;
import ravioli.gravioli.dialogue.attach.AttachmentHandler;
import ravioli.gravioli.dialogue.attach.CitizensAttachmentHandler;
import ravioli.gravioli.dialogue.command.DialogueCommand;
import ravioli.gravioli.dialogue.data.DialogueDao;
import ravioli.gravioli.dialogue.listener.DialogueInputListeners;
import ravioli.gravioli.dialogue.listener.DialogueListeners;
import ravioli.gravioli.dialogue.registry.DialogueRegistry;
import ravioli.gravioli.dialogue.service.DialogueService;
import ravioli.gravioli.dialogue.service.RavioliDialogueService;
import ravioli.gravioli.dialogue.task.InvalidConversationCheckTask;
import ravioli.gravioli.dialogue.text.ActionableDialogueComponent;
import ravioli.gravioli.dialogue.text.OptionDialogueComponent;
import ravioli.gravioli.dialogue.text.TextDialogueComponent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class DialoguePlugin extends RavioliPlugin {
    private final DialogueDao dialogueDao;
    private final RavioliDialogueService dialogueService;
    private final DialogueInputListeners dialogueInputListeners;
    private final InvalidConversationCheckTask invalidConversationCheckTask;

    private final Map<String, AttachmentHandler> attachmentHandlers = new HashMap<>();
    private final BukkitCommandManager commandManager;

    public DialoguePlugin() {
        this.saveDefaultConfig();

        this.dialogueDao = new DialogueDao(this.postgresProvider());
        this.dialogueService = new RavioliDialogueService(this.dialogueDao);
        this.dialogueInputListeners = new DialogueInputListeners(this.dialogueService, this);
        this.invalidConversationCheckTask = new InvalidConversationCheckTask(this.dialogueService);
        this.commandManager = new BukkitCommandManager(this);

        Platform.registerService(DialogueService.class, this.dialogueService);
    }

    @Override
    protected void onPluginLoad() {
        this.executeScripts();

        DialogueRegistry.REGISTRY.registerComponent("action", ActionableDialogueComponent.class);
        DialogueRegistry.REGISTRY.registerComponent("text", TextDialogueComponent.class);
        DialogueRegistry.REGISTRY.registerComponent("option", OptionDialogueComponent.class);
        DialogueRegistry.REGISTRY.registerAction("command", CommandDialogueAction.class);

        this.registerAttachmentHandlers();
        this.loadAttachments();
    }

    @Override
    protected void onPluginEnable() {
        Bukkit.getPluginManager().registerEvents(new DialogueListeners(this.dialogueService, this.dialogueDao), this);
        SchedulerUtil.sync().runInterval(this.invalidConversationCheckTask, 5, 5);

        this.commandManager.register(new DialogueCommand(this.getDataFolder(), this.dialogueService));

        ProtocolLibrary.getProtocolManager().addPacketListener(this.dialogueInputListeners);
    }

    @Override
    protected void onPluginDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this.dialogueInputListeners);
    }

    private void registerAttachmentHandlers() {
        final PluginManager pluginManager = Bukkit.getPluginManager();

        if (pluginManager.isPluginEnabled("Citizens")) {
            final CitizensAttachmentHandler attachmentHandler = new CitizensAttachmentHandler(this.getDataFolder());

            this.attachmentHandlers.put("citizens-npc", attachmentHandler);

            Bukkit.getPluginManager().registerEvents(attachmentHandler, this);
        }
    }

    private void loadAttachments() {
        final FileConfiguration config = this.getConfig();
        final ConfigurationSection attachmentsSection = config.getConfigurationSection("dialogue-attachment");

        if (attachmentsSection == null) {
            return;
        }
        for (final String key : attachmentsSection.getKeys(false)) {
            final ConfigurationSection section =
                Objects.requireNonNull(attachmentsSection.getConfigurationSection(key));
            final String type = Objects.requireNonNull(section.getString("type"));
            final AttachmentHandler attachmentHandler = this.attachmentHandlers.get(type);

            if (attachmentHandler == null) {
                Bukkit.getLogger().log(Level.WARNING, "Invalid attachment type \"" + type + "\".");

                continue;
            }
            attachmentHandler.registerAttachment(section);
        }
    }

    private void executeScripts() {
        try {
            this.postgresProvider().executeScript(Objects.requireNonNull(this.getResource("mysql/dialogue.sql")));
        } catch (final SQLException | IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to execute MySQL scripts.", e);

            throw new RuntimeException(e);
        }
    }
}
