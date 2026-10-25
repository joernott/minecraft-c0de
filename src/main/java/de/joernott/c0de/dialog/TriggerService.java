package de.joernott.c0de.dialog;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.compat.Compat;
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
                        .ifPresent(problem -> Compat.message(
                                player, Component.translatable("message.c0de.not_saved", problem), true)));
    }

    /**
     * Changes the configuration of a trigger block.
     *
     * @return the reason if it was not changed
     */
    public static Optional<String> configure(ServerPlayer player, TriggerConfigPayload payload) {
        if (!Compat.level(player).isLoaded(payload.pos()) || !Compat.canReach(player, payload.pos(), 2.0)) {
            return Optional.of("The block is too far away");
        }
        if (!(Compat.level(player).getBlockEntity(payload.pos()) instanceof TriggerBlockEntity trigger)) {
            return Optional.of("The block is not a trigger block");
        }
        return trigger.configure(
                player,
                payload.program(),
                payload.function(),
                payload.interval(),
                TriggerBlockEntity.Run.of(payload.run()));
    }
}
