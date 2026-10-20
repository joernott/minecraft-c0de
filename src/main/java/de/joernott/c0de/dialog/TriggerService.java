package de.joernott.c0de.dialog;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.network.TriggerConfigPayload;
import de.joernott.c0de.platform.Platform;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** The server side of the dialog of trigger blocks. */
public final class TriggerService {
    private TriggerService() {}

    /** Registers the payload of the configuration. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.registerServerbound(
                TriggerConfigPayload.TYPE,
                TriggerConfigPayload.CODEC,
                (payload, player) -> configure(player, payload)
                        .ifPresent(problem -> player.displayClientMessage(
                                Component.translatable("message.c0de.not_saved", problem), true)));
    }

    /**
     * Changes the configuration of a trigger block.
     *
     * @return the reason if it was not changed
     */
    public static Optional<String> configure(ServerPlayer player, TriggerConfigPayload payload) {
        if (!player.serverLevel().isLoaded(payload.pos()) || !player.canInteractWithBlock(payload.pos(), 2.0)) {
            return Optional.of("The block is too far away");
        }
        if (!(player.serverLevel().getBlockEntity(payload.pos()) instanceof TriggerBlockEntity trigger)) {
            return Optional.of("The block is not a trigger block");
        }
        return trigger.configure(player, payload.program(), payload.function(), payload.interval());
    }
}
