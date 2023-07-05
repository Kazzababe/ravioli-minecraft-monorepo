package ravioli.gravioli.dialogue.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.dialogue.attach.AttachmentHandler;
import ravioli.gravioli.dialogue.config.ConversationConfig;
import ravioli.gravioli.dialogue.data.DialogueDao;
import ravioli.gravioli.dialogue.model.BukkitDialogue;
import ravioli.gravioli.dialogue.model.Dialogue;
import ravioli.gravioli.dialogue.model.DialogueGroup;
import ravioli.gravioli.dialogue.model.DialogueKey;
import ravioli.gravioli.dialogue.model.conversation.Conversation;
import ravioli.gravioli.dialogue.registry.DialogueRegistry;
import ravioli.gravioli.dialogue.requirement.DialogueRequirement;
import ravioli.gravioli.dialogue.source.DialogueSource;
import ravioli.gravioli.dialogue.text.DialogueComponent;
import ravioli.gravioli.dialogue.text.TextDialogueComponent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class RavioliDialogueService implements DialogueService {
    private final DialogueDao dialogueDao;

    private final Map<UUID, List<DialogueGroup>> attachedDialogue = new ConcurrentHashMap<>();
    private final Map<DialogueKey, Integer> dialogueStateCache = new ConcurrentHashMap<>();
    private final Map<UUID, Conversation> currentPlayerConversation = new HashMap<>();

    public RavioliDialogueService(@NotNull final DialogueDao dialogueDao) {
        this.dialogueDao = dialogueDao;
    }

    public @Nullable List<DialogueGroup> getAttachedDialogue(@NotNull final UUID entityId) {
        return this.attachedDialogue.get(entityId);
    }

    public int getDialogueState(@NotNull final Player player, @NotNull final String id) {
        return this.dialogueStateCache.getOrDefault(new DialogueKey(player.getUniqueId(), id), 0);
    }

    public void invalidateUser(@NotNull final Player player) {
        final UUID uuid = player.getUniqueId();

        this.dialogueStateCache
            .entrySet()
            .removeIf(entry -> entry.getKey().uuid().equals(uuid));

        this.getCurrentConversation(uuid).ifPresent(Conversation::stop);
        this.clearCurrentConversation(uuid);
    }

    @Override
    public void attachDialogue(@NotNull final Entity entity, @NotNull final DialogueGroup dialogueGroup) {
        this.attachedDialogue
            .computeIfAbsent(entity.getUniqueId(), (key) -> new ArrayList<>())
            .add(dialogueGroup);
    }

    @Override
    public void unattachDialogue(@NotNull final Entity entity, @NotNull final DialogueGroup dialogueGroup) {
        final List<DialogueGroup> dialogues = this.attachedDialogue.get(entity.getUniqueId());

        if (dialogues == null) {
            return;
        }
        dialogues.remove(dialogueGroup);

        if (dialogues.isEmpty()) {
            this.attachedDialogue.remove(entity.getUniqueId());
        }
    }

    @Override
    public @NotNull List<@NotNull Entity> getAttachedEntities(@NotNull DialogueGroup dialogueGroup) {
        return this.attachedDialogue.entrySet().stream()
            .filter((entry) -> entry.getValue().contains(dialogueGroup))
            .map((entry) -> Bukkit.getEntity(entry.getKey()))
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public @NotNull DialogueGroup loadDialogue(@NotNull final File file) throws IOException {
        final Map<String, BukkitDialogue> dialogues = new HashMap<>();

        try (final FileReader fileReader = new FileReader(file)) {
            final JsonArray jsonArray = JsonParser.parseReader(fileReader).getAsJsonArray();

            for (final JsonElement jsonElement : jsonArray) {
                if (!jsonElement.isJsonObject()) {
                    continue;
                }
                final JsonObject rootJson = jsonElement.getAsJsonObject();

                if (!rootJson.has("id")) {
                    Bukkit.getLogger().log(Level.WARNING, "No \"id\" property found on dialogue.");

                    continue;
                }
                if (!rootJson.has("dialogue")) {
                    Bukkit.getLogger().log(Level.WARNING, "No \"dialogue\" property found on dialogue.");

                    continue;
                }
                final String id = rootJson.get("id").getAsString();
                final JsonArray dialogueArray = rootJson.getAsJsonArray("dialogue");
                final List<DialogueRequirement> requirements = new ArrayList<>();
                final List<ConversationConfig[]> allConversations = new ArrayList<>();

                if (rootJson.has("requirements")) {
                    final JsonArray requirementsArray = rootJson.getAsJsonArray("requirements");

                    for (final JsonElement requirementElement : requirementsArray) {
                        final JsonObject requirementJson = requirementElement.getAsJsonObject();
                        final String type = requirementJson.get("type").getAsString();
                        final DialogueRequirement requirement;

                        try {
                            requirement = DialogueRegistry.REGISTRY.parseRequirement(type, requirementJson);
                        } catch (final Exception e) {
                            Bukkit.getLogger().log(Level.WARNING, "Unable to parse dialogue requirement.", e);

                            continue;
                        }
                        requirements.add(requirement);
                    }
                }
                for (final JsonElement jsonElement2 : dialogueArray) {
                    if (!jsonElement2.isJsonArray()) {
                        continue;
                    }
                    final JsonArray conversationsJson = jsonElement2.getAsJsonArray();
                    final List<ConversationConfig> conversations = new ArrayList<>();

                    for (final JsonElement jsonElement3 : conversationsJson) {
                        if (!jsonElement3.isJsonArray()) {
                            continue;
                        }
                        final JsonArray conversationJson = jsonElement3.getAsJsonArray();
                        final List<DialogueComponent> dialogueComponents = new ArrayList<>();

                        for (final JsonElement jsonElement4 : conversationJson) {
                            if (jsonElement4.isJsonObject()) {
                                final JsonObject textJson = jsonElement4.getAsJsonObject();

                                if (!textJson.has("type")) {
                                    Bukkit.getLogger()
                                        .log(
                                            Level.WARNING,
                                            "JSON object supplied in conversation, but no \"type\" property found.");

                                    continue;
                                }
                                final String type = textJson.get("type").getAsString();
                                final DialogueComponent dialogueComponent;

                                try {
                                    dialogueComponent = DialogueRegistry.REGISTRY.parseComponent(type, textJson);
                                } catch (final Exception e) {
                                    Bukkit.getLogger().log(Level.WARNING, "Unable to parse dialogue component.", e);

                                    continue;
                                }
                                dialogueComponents.add(dialogueComponent);
                            } else if (jsonElement4.isJsonPrimitive()) {
                                final String text = jsonElement4.getAsString();

                                dialogueComponents.add(new TextDialogueComponent(text));
                            }
                        }
                        final ConversationConfig conversation = new ConversationConfig(dialogueComponents.toArray(DialogueComponent[]::new));

                        conversations.add(conversation);
                    }
                    allConversations.add(conversations.toArray(ConversationConfig[]::new));
                }
                final BukkitDialogue dialogue = new BukkitDialogue(
                    id,
                    requirements,
                    allConversations.toArray(ConversationConfig[][]::new)
                );

                dialogues.put(id, dialogue);
            }
        }
        if (dialogues.isEmpty()) {
            throw new IOException("No dialogue was found in this file.");
        }
        return new DialogueGroup(dialogues);
    }

    @Override
    public boolean processDialogue(@NotNull final Player player,
                                   @NotNull final DialogueGroup dialogueGroup,
                                   @NotNull final DialogueSource dialogueSource) {
        final UUID uuid = player.getUniqueId();
        final Conversation currentConversation = this.currentPlayerConversation.get(uuid);

        if (currentConversation != null) {
            if (currentConversation.parentDialogueGroup().equals(dialogueGroup)) {
                final Conversation.NextResult nextResult = currentConversation.next();

                switch (nextResult) {
                    case PROCEED -> {
                        final String dialogueId = currentConversation.parentDialogue().getId();
                        final DialogueKey key = new DialogueKey(uuid, dialogueId);
                        final int currentDialogueState = this.dialogueStateCache.getOrDefault(key, 0);
                        final int newDialogueState = Math.min(currentConversation.parentDialogue().getConversations().size() - 1, currentDialogueState + 1);

                        this.clearCurrentConversation(uuid);

                        this.dialogueStateCache.put(key, newDialogueState);
                        this.dialogueDao.setDialogueState(player.getUniqueId(), dialogueId, newDialogueState);
                    }
                    case END -> {
                        this.clearCurrentConversation(uuid);
                    }
                }
                return true;
            } else {
                currentConversation.stop();

                this.currentPlayerConversation.remove(uuid);
            }
        }
        final Dialogue dialogue = this.findBestDialogue(player, dialogueGroup);

        if (dialogue == null) {
            return false;
        }
        final DialogueKey key = new DialogueKey(player.getUniqueId(), dialogue.getId());
        final int dialogueState = this.dialogueStateCache.getOrDefault(key, 0);
        final List<ConversationConfig[]> dialogueConversations = dialogue.getConversations();

        if (dialogueState >= dialogueConversations.size()) {
            return false;
        }
        final ConversationConfig[] conversations = dialogue.getConversations().get(dialogueState);
        final ConversationConfig conversationConfig = conversations[ThreadLocalRandom.current().nextInt(conversations.length)];
        final Conversation conversation = conversationConfig.createConversation(player, dialogueGroup, dialogue, dialogueSource);

        this.setCurrentConversation(uuid, conversation);

        return this.processDialogue(player, dialogueGroup, dialogueSource);
    }

    private @Nullable Dialogue findBestDialogue(@NotNull final Player player, @NotNull final DialogueGroup dialogueGroup) {
        final List<PriorityDialogue> priorityDialogues = new ArrayList<>();

        for (final Dialogue dialogue : dialogueGroup.dialogues().values()) {
            final List<DialogueRequirement> requirements = dialogue.getRequirements();

            if (requirements.isEmpty()) {
                priorityDialogues.add(new PriorityDialogue(null, 0, dialogue));

                continue;
            }
            int highestPriority = 0;
            DialogueRequirement highestRequirement = null;

            for (final DialogueRequirement requirement : requirements) {
                if (!requirement.valid(player)) {
                    highestRequirement = null;

                    break;
                }
                final int priority = requirement.getPriority();

                if (highestRequirement == null || priority > highestPriority) {
                    highestPriority = priority;
                    highestRequirement = requirement;
                }
            }
            if (highestRequirement == null) {
                continue;
            }
            priorityDialogues.add(new PriorityDialogue(highestRequirement, highestPriority, dialogue));
        }
        priorityDialogues.sort(Comparator.comparing(PriorityDialogue::priority));

        return priorityDialogues.stream()
            .sorted(Comparator.comparing(PriorityDialogue::priority).reversed())
            .filter(priorityDialogue ->
                priorityDialogue.requirement == null || priorityDialogue.requirement.valid(player))
            .findFirst()
            .map(PriorityDialogue::dialogue)
            .orElse(null);
    }

    public void setCurrentConversation(@NotNull final UUID uuid, @NotNull final Conversation conversation) {
        this.currentPlayerConversation.put(uuid, conversation);
    }

    public @NotNull Optional<@NotNull Conversation> getCurrentConversation(@NotNull final UUID uuid) {
        return Optional.ofNullable(this.currentPlayerConversation.get(uuid));
    }

    public void clearCurrentConversation(@NotNull final UUID uuid) {
        this.currentPlayerConversation.remove(uuid);
    }

    public void setPlayerStates(@NotNull final Map<DialogueKey, Integer> stateMap) {
        this.dialogueStateCache.putAll(stateMap);
    }

    private record PriorityDialogue(@Nullable DialogueRequirement requirement, int priority, @NotNull Dialogue dialogue) {}
}
