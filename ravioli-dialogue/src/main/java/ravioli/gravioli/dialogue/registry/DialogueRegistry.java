package ravioli.gravioli.dialogue.registry;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.dialogue.action.DialogueAction;
import ravioli.gravioli.dialogue.requirement.DialogueRequirement;
import ravioli.gravioli.dialogue.text.DialogueComponent;

import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DialogueRegistry {
    public static DialogueRegistry REGISTRY = new DialogueRegistry();

    private final Map<String, Function<JsonObject, DialogueAction>> registeredActions = new HashMap<>();
    private final Map<String, Function<JsonObject, DialogueRequirement>> registeredRequirements = new HashMap<>();
    private final Map<String, Function<JsonObject, DialogueComponent>> registeredComponents = new HashMap<>();

    public @Nullable DialogueAction parseAction(@NotNull final String id, @NotNull final JsonObject jsonObject) {
        final Function<JsonObject, DialogueAction> parseFunction = this.registeredActions.get(id);

        if (parseFunction == null) {
            return null;
        }
        return parseFunction.apply(jsonObject);
    }

    public @Nullable DialogueRequirement parseRequirement(
            @NotNull final String id, @NotNull final JsonObject jsonObject) {
        final Function<JsonObject, DialogueRequirement> parseFunction = this.registeredRequirements.get(id);

        if (parseFunction == null) {
            return null;
        }
        return parseFunction.apply(jsonObject);
    }

    public @Nullable DialogueComponent parseComponent(@NotNull final String id, @NotNull final JsonObject jsonObject) {
        final Function<JsonObject, DialogueComponent> parseFunction = this.registeredComponents.get(id);

        if (parseFunction == null) {
            return null;
        }
        return parseFunction.apply(jsonObject);
    }

    public void registerAction(@NotNull final String id, @NotNull final Class<? extends DialogueAction> actionClass) {
        try {
            final Method parseMethod = actionClass.getDeclaredMethod("parse", JsonObject.class);

            if (parseMethod.getReturnType() != actionClass) {
                throw new WrongMethodTypeException("Invalid return type of action parse method.");
            }
            this.registeredActions.put(id, (jsonObject) -> {
                try {
                    return (DialogueAction) parseMethod.invoke(null, jsonObject);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (final NoSuchMethodException | WrongMethodTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerRequirement(
            @NotNull final String id, @NotNull final Class<? extends DialogueRequirement> actionClass) {
        try {
            final Method parseMethod = actionClass.getDeclaredMethod("parse", JsonObject.class);

            if (parseMethod.getReturnType() != actionClass) {
                throw new WrongMethodTypeException("Invalid return type of requirement parse method.");
            }
            this.registeredRequirements.put(id, (jsonObject) -> {
                try {
                    return (DialogueRequirement) parseMethod.invoke(null, jsonObject);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (final NoSuchMethodException | WrongMethodTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerComponent(
            @NotNull final String id, @NotNull final Class<? extends DialogueComponent> actionClass) {
        try {
            final Method parseMethod = actionClass.getDeclaredMethod("parse", JsonObject.class);

            if (parseMethod.getReturnType() != actionClass) {
                throw new WrongMethodTypeException("Invalid return type of component parse method.");
            }
            this.registeredComponents.put(id, (jsonObject) -> {
                try {
                    return (DialogueComponent) parseMethod.invoke(null, jsonObject);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (final NoSuchMethodException | WrongMethodTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
